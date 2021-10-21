"""
Routes and views for the flask application.
"""

from datetime import datetime
from flask import Flask, render_template, request, jsonify
import firebase_admin
from firebase_admin import firestore
from firebase_admin import credentials
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import linear_kernel
from nltk.corpus import stopwords
import string
import joblib

app = Flask(__name__)

cred = credentials.Certificate("serviceAccountKey.json")
firebase_admin.initialize_app(cred)

db = firestore.client()

# read dataframe
product_dataframe = pd.read_excel("product_data_sliced.xlsx")
product_dataframe['combined_features'] = product_dataframe.brand + ' ' + product_dataframe.category + ' ' + product_dataframe.ingredients
review_dataframe = pd.read_excel("user_reviews_sliced.xlsx")

# reading the models
knn = joblib.load('knn.sav')
dt = joblib.load('dt.sav')
    
# stop words filtering and vectorizing
stop = set(stopwords.words('english') + list(string.punctuation)) # creating stop word dictionary
tf = TfidfVectorizer(analyzer='word', ngram_range=(1, 2), min_df=0, stop_words=stop) # importing Tf-idf vectorizer
tfidf_matrix = tf.fit_transform(product_dataframe.combined_features.values.astype('U')) # creating a matrix for Tf-idf values

# cosine similarity
cosine_similarities = linear_kernel(tfidf_matrix, tfidf_matrix) # constructing cosine similarity kernel
results = {} # dictionary to hold values
for idx, row in product_dataframe.iterrows(): # for loop to find distance of each vector
    similar_indices = cosine_similarities[idx].argsort()[:-100:-1] 
    similar_items = [(cosine_similarities[idx][i], product_dataframe['product_id'][i]) for i in similar_indices] 
    results[row['product_id']] = similar_items[1:]

# item function to retrieve product name from the main dataframe
def item(id):
    global product_dataframe
    return product_dataframe.loc[product_dataframe['product_id'] == id]['product_name'].tolist()[0]

# recommendation function to get the smallest cosine distance from the entered item_id
# it will generate num amount of suggestions
# user_id is the current signed in user ID
def recommend(item_id, num, user_id):
    global results
    print()
    print("Recommended " + str(num) + " products similar to " + str(item_id) + ": " + item(item_id) + " for user " + str(user_id) + ": ") # printing logs
    recs = results[item_id][:num]   # getting the specified number of items beyond the item ID
    mean = [] # creating a list for calculating mean for logs
    items = [] # creating a list for storing to the database
    for rec in recs: # iterating the recs list
        print(str(rec[1]) + ": " + item(rec[1]) + " (score:" + str(rec[0]) + ")") # logging the results
        mean.append(float(rec[0])) # mean calculated
        items.append(int(rec[1])) # items list appended
    print("Average: ", sum(mean)/len(mean)) # average calculated

    try: # try to update the recommendation list if already created
        db.collection('recommendations').document(user_id).update({'content': firestore.ArrayUnion(items)})
    except: # create the recommendation list if it not created
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
        db.collection('recommendations').document(user_id).set({'collaborative': item_id})

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
    suggestion = []

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

    docs = db.collection('Users').document(str(user_id)).collection('ratings').get() # get user's product rating
    if len(docs) > 0: # if user has rated some items
        suggestion = [] # list for storing results
        for review in docs: # iterating thourgh the records and storing data
            item = review.to_dict()
            rating = item['rating']
            user = item['user_id']
            tempList = [rating, user]
            suggestion.append(tempList) # appending the features

        knn_result = knn.predict(suggestion) # predicting from the features
        knn_result = knn_result.tolist() # convert the suggestion to a list for storing to Firebase
        print(suggestion)
        print(knn_result)
        try: # update collaborative filtering if document exists
            db.collection('recommendations').document(user_id).update({'collaborative': firestore.ArrayUnion(knn_result)})
        except: # create array if it does not exist yet
            db.collection('recommendations').document(user_id).set({'collaborative': firestore.ArrayUnion(knn_result)})
    else:
        print("User has liked no item for KNN")

    docs = db.collection('Users').document(str(user_id)).get() # reading user characters from the database
    suggestion = [] # suggestion list to store results
    item = docs.to_dict() # converting retrieved database records to a dictionary
    combination_skin = item['combination_skin'] # storing all the features in a variable
    oily_skin = item['oily_skin']
    sensitive_skin = item['sensitive_skin']
    dry_skin = item['dry_skin']
    normal_skin = item['normal_skin']
    tempList = [combination_skin, dry_skin, normal_skin, oily_skin, sensitive_skin] # appending all the characteristics to a list for prediction
    suggestion.append(tempList)
    print(suggestion) # logging results
    dt_result = dt.predict(suggestion) # prediction
    try: # update collaborative filtering if document exists
        db.collection('recommendations').document(user_id).update({'collaborative': firestore.ArrayUnion(dt_result)})
    except: # create array if it does not exist yet
        db.collection('recommendations').document(user_id).set({'collaborative': firestore.ArrayUnion(dt_result)})

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

if __name__=="__main__":
	app.run(debug=False)
