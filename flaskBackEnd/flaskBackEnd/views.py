"""
Routes and views for the flask application.
"""

from datetime import datetime
from flask import render_template, request, jsonify
from flaskBackEnd import app
import firebase_admin
from firebase_admin import firestore
from firebase_admin import credentials
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import linear_kernel
from nltk.corpus import stopwords
import string
import joblib

cred = credentials.Certificate("serviceAccountKey.json")
firebase_admin.initialize_app(cred)

db = firestore.client()

# read dataframe
product_dataframe = pd.read_excel("product_data_sliced.xlsx")
product_dataframe['combined_features'] = product_dataframe.brand + ' ' + product_dataframe.category + ' ' + product_dataframe.ingredients
review_dataframe = pd.read_excel("user_reviews_sliced.xlsx")
knn = joblib.load('knn.sav')
    
# stop words filtering and vectorizing
stop = set(stopwords.words('english') + list(string.punctuation))
tf = TfidfVectorizer(analyzer='word', ngram_range=(1, 2), min_df=0, stop_words=stop)
tfidf_matrix = tf.fit_transform(product_dataframe.combined_features.values.astype('U'))

# cosine similarity
cosine_similarities = linear_kernel(tfidf_matrix, tfidf_matrix) 
results = {}
for idx, row in product_dataframe.iterrows():
    similar_indices = cosine_similarities[idx].argsort()[:-100:-1] 
    similar_items = [(cosine_similarities[idx][i], product_dataframe['product_id'][i]) for i in similar_indices] 
    results[row['product_id']] = similar_items[1:]

def item(id):
    global product_dataframe
    return product_dataframe.loc[product_dataframe['product_id'] == id]['product_name'].tolist()[0]

def recommend(item_id, num, user_id):
    global results
    print()
    print("Recommended " + str(num) + " products similar to " + str(item_id) + ": " + item(item_id) + " for user " + str(user_id) + ": ")
    print("-------")    
    recs = results[item_id][:num]   
    mean = []
    items = []
    for rec in recs: 
        print(str(rec[1]) + ": " + item(rec[1]) + " (score:" + str(rec[0]) + ")")
        mean.append(float(rec[0]))
        items.append(int(rec[1]))
    print("Average: ", sum(mean)/len(mean))

    try:
        db.collection('recommendations').document(user_id).update({'content': firestore.ArrayUnion(items)})
    except:
        db.collection('recommendations').document(user_id).set({'content': items})

def recommendCBF(item_id, num, user_id, like_item):
    print()
    print("For user: " + str(user_id) + " Users who bought " + str(like_item) + ": " + product_dataframe['product_name'][product_dataframe['product_id'] == like_item].item() + " also bought:")
    print("-------")
    for rec in item_id: 
        print(str(rec) + ": " + item(rec))

    try:
        db.collection('recommendations').document(user_id).update({'collaborative': firestore.ArrayUnion(item_id)})
    except:
        db.collection('recommendations').document(user_id).set({'collaborative': firestore.ArrayUnion(item_id)})

@app.route('/')
@app.route('/home')
def home():
    """Renders the home page."""
    return render_template(
        'index.html',
        title='Home Page',
        year=datetime.now().year,
    )

@app.route('/contact')
def contact():
    """Renders the contact page."""
    return render_template(
        'contact.html',
        title='Contact',
        year=datetime.now().year,
        message='Your contact page.'
    )

@app.route('/about')
def about():
    """Renders the about page."""
    return render_template(
        'about.html',
        title='About',
        year=datetime.now().year,
        message='Your application description page.'
    )

@app.route('/contentBasedFiltering')
def contentBasedFiltering():
    user_id = request.args.get('user_id')
    response = 200
    msg = 'Content filtering refreshed'
    if user_id is None:
        response = 400
        msg = 'No user ID'
        return jsonify(response=response, message=msg)

    like_list = db.collection('Users').document(user_id).get().to_dict()      

    #recommend
    try:
        for like_item in like_list['likedProducts']:
            try:
                recommend(item_id=like_item, num=2, user_id=user_id)
                response = 200
                msg = 'Recommendation refreshed for user ID ' + str(user_id)
            except:
                print(f'No viable recommendation for product ID {like_item}')
                response = 200
                msg = 'No viable recommendation found for user ID ' + str(user_id)
                continue
    except:
        response = 400
        msg = 'No like list found for user ID ' + str(user_id)
        return jsonify(response=response, message=msg)
        
   
    return jsonify(response=response, message=msg)

@app.route('/collaborativeBasedFiltering')
def collaborativeBasedFiltering():
    global review_dataframe
    response = 200
    msg = 'Collaborative filtering refreshed'
    user_id = request.args.get('user_id')
    if user_id is None:
        response = 400
        msg = 'No user ID'
        return jsonify(response=response, message=msg)

    userRatings_dataframe = review_dataframe[['user_id', 'product_id', 'stars']]

    # read new user data
    docs = db.collection('Users').where('user_id', 'not-in', ['LfQFPMkqQ4bp07N0awGc4Gn6i2K2', 'bWhaebb57UUQ6Xz5bfTnTtf3UX92'] ).get()
    for doc in docs:
        ratings = db.collection('Users').document(str(doc.id)).collection('ratings').get()
        for collection in ratings:
            item = collection.to_dict()
            product_ids = [item['productId']]
            rating = [item['rating']]
            user_ids = [item['user_id']]
            tempUser_dataframe = pd.DataFrame({'user_id': user_ids, 'product_id': product_ids, 'stars': rating})
            userRatings_dataframe = userRatings_dataframe.append(tempUser_dataframe, ignore_index=True)

    docs = db.collection('Users').document(str(user_id)).collection('ratings').get()
    if len(docs) > 0:
        suggestion = []
        for review in docs:
            item = review.to_dict()
            rating = item['rating']
            user = item['user_id']
            tempList = [rating, user]
            suggestion.append(tempList)

        knn_result = knn.predict(suggestion)
        knn_result = knn_result.tolist()
        print(suggestion)
        print(knn_result)
        try:
            db.collection('recommendations').document(user_id).update({'collaborative': firestore.ArrayUnion(knn_result)})
        except:
            db.collection('recommendations').document(user_id).set({'collaborative': firestore.ArrayUnion(knn_result)})
    else:
        print("User has liked no item for KNN")

    # correlation matrix
    star_matrix = userRatings_dataframe.groupby(['user_id', 'product_id'])['stars'].sum().unstack().reset_index().fillna(0).set_index('user_id')
    drop_list = []
    for i in star_matrix.index:
        total_ratings = sum([1 for i in star_matrix.loc[i] if i != 0.0])
        if total_ratings < 2:
            drop_list.append(i)
    star_matrix.drop(index = drop_list, inplace = True)

    #recommend
    like_list = db.collection('Users').document(user_id).get().to_dict()
        
    try:
        for like_item in like_list['likedProducts']:
            try:
                item_specific = star_matrix[like_item]
                similar_rating = star_matrix.corrwith(item_specific)
                star_series = pd.DataFrame(similar_rating[similar_rating > 0][1:])
                star_series.reset_index(inplace=True)
                star_list = []
                star_list.append(int(star_series['product_id'][0]))
                star_list.append(int(star_series['product_id'][1]))
                recommendCBF(star_list, 2, user_id, like_item)
                response = 200
                msg = 'Recommendation refreshed for user ID ' + str(user_id)
            except:
                print(f'No viable recommendation for product ID {like_item}')
                response = 200
                msg = 'No viable recommendation found for user ID ' + str(user_id)
                continue
    except:
        response = 400
        msg = 'No like list found for user ID ' + str(user_id)
        return jsonify(response=response, message=msg)

    return jsonify(response=response, message=msg)
