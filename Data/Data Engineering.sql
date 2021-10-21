CREATE DATABASE SkincareRecommendation;

SELECT *
FROM skincare_products;

-- Main select query for data

-- User reviews data
SELECT TRY_CAST(user_id AS int) AS [user_id]
	,TRY_CAST(SUBSTRING(product_id, 1, CHARINDEX('.', product_id)-1) AS int) AS [product_id]
	,username
	,TRY_CAST(stars AS smallint) AS [stars]
	,review
	,review_cleaned
	,TRY_CAST(combination_skin AS smallint) AS [combination_skin]
	,TRY_CAST(dry_skin AS smallint) AS [dry_skin]
	,TRY_CAST(normal_skin AS smallint) AS [normal_skin]
	,TRY_CAST(oily_skin AS smallint) AS [oily_skin]
	,TRY_CAST(sensitive_skin AS smallint) AS [sensitive_skin]
	,skin_tone
	,skin_type
	,eye_color
	,hair_color
FROM user_reviews_multiple;

-- Product data
SELECT TRY_CAST(product_id AS int) AS [product_id]
	,product_name
	,product_url
	,image_url
	,category
	,ingredients
	,TRY_CAST(price AS float) AS [price]
	,brand
	,TRY_CAST(TRY_CAST(rating AS float) AS int) AS [Rating]
	,TRY_CAST(good_reviews AS int) AS [good_reviews]
	,TRY_CAST(bad_reviews AS int) AS [bad_reviews]
	,description
FROM products_sliced;

-- Cleaning and preparing user_reviews
-- User review data

SELECT DISTINCT rating
FROM skincare_products;

SELECT *
FROM user_reviews
WHERE Username = 'severus';

UPDATE user_reviews
SET username = 'severus'
WHERE username = '';

SELECT *
FROM user_reviews
WHERE product_id = 'nan';

DELETE FROM user_reviews
WHERE product_id = 'nan';

SELECT *
FROM user_reviews
WHERE review = 'nan';

UPDATE user_reviews
SET review = 'absorbs easily mild enough sensitive skin heavy smell non drying advertised'
WHERE review = 'nan';

SELECT *
FROM user_reviews
WHERE review_cleaned = 'nan';

UPDATE user_reviews
SET review_cleaned = ''
WHERE review_cleaned = 'nan';

SELECT *
FROM user_reviews
WHERE skin_tone = 'No data';

UPDATE user_reviews
SET skin_tone = ''
WHERE skin_tone = 'No data';

-- Product data

SELECT product_name, COUNT(product_name) AS [Count]
FROM skincare_products
GROUP BY product_name
HAVING COUNT(product_name) > 1;

SELECT *
FROM skincare_products
WHERE product_name = '100 percent Pure Argan Oil';

SELECT product_url, image_url, ingredients
FROM skincare_products
WHERE product_url = 'nan'
	AND image_url = 'nan';

UPDATE skincare_products
SET product_url = 'https://www.lookfantastic.com/the-ordinary-natural-moisturising-factors-ha-30ml/11396687.html'
	,image_url = 'https://static.thcdn.com/images/large/original//productimg/1600/1600/11396687-5114817633423328.jpg'
WHERE product_url = 'nan'
	AND image_url = 'nan';

SELECT product_url, image_url
FROM skincare_products
WHERE image_url = 'nan';

UPDATE skincare_products
SET image_url = 'https://static.thcdn.com/images/large/original//productimg/1600/1600/11363395-8324817633335596.jpg'
WHERE image_url = 'nan';

--SELECT * INTO skincare_product_bkp1 FROM skincare_products;

SELECT ISNULL(brand, 'Brand Total') AS [Brand], COUNT(*) AS [Count]
FROM skincare_products
GROUP BY ROLLUP(brand)
HAVING COUNT(*) = 1;

DELETE FROM skincare_products
WHERE brand IN
(
	SELECT ISNULL(brand, 'Brand Total') AS [Brand]
	FROM skincare_products
	GROUP BY ROLLUP(brand)
	HAVING COUNT(*) = 1
);

UPDATE skincare_products
SET category = CASE category
	WHEN 'Mask' THEN 'Face Mask'
	WHEN 'Bath Oil' THEN 'Bath Salts'
	WHEN 'Eye cream' THEN 'Eye Care'
	WHEN 'Moisturiser' THEN 'Moisturizer'
	WHEN 'Peel' THEN 'Face Mask'
	WHEN 'Oil' THEN 'Serum'
	WHEN 'Exfoliator' THEN 'Face Mask'
	WHEN 'Balm' THEN 'Treatment'
	WHEN 'Mist' THEN 'Moisturizer'
	ELSE category
	END;

UPDATE skincare_products
SET brand = CASE brand
	WHEN 'Clinique for Men' THEN 'Clinique'
	WHEN 'ESTÃ‰E LAUDER' THEN 'ESTÉE LAUDER'
	WHEN 'EstÃ©e Lauder' THEN 'ESTÉE LAUDER'
	WHEN 'FARSÃLI' THEN 'FARSÁLI'
	WHEN 'LANCÃ”ME' THEN 'LancÃ´me'
	WHEN 'LOrÃ©al Paris Men Expert' THEN 'L''oreal Paris'
	WHEN 'L''OrÃ©al Paris' THEN 'L''oreal Paris'
	WHEN 'Lancer Skincare' THEN 'LANCER'
	WHEN 'Dr. Brandt' THEN 'DR. BRANDT SKINCARE'
	ELSE brand
	END;


SELECT *
FROM skincare_products
WHERE ingredients = 'NO INFO';

SELECT *
FROM user_reviews
WHERE product_id IN ('2470'
	,'1493'
	,'1888'
	,'1545'
	,'1596'
	,'1613'
	,'1667'
	,'1717'
	,'1726'
	,'1849'
	,'1950'
	,'2457'
	,'2110'
	,'2134'
	,'2203'
	,'2408'
	,'2419'
	,'2425'
	,'2395'
	,'2397'
	,'2477'
	,'2478'
	,'2483'
	,'2485'
	,'2469');

DELETE FROM skincare_products
WHERE product_id IN ('2470'
	,'1493'
	,'1888'
	,'1545'
	,'1596'
	,'1613'
	,'1667'
	,'1717'
	,'1726'
	,'1849'
	,'1950'
	,'2457'
	,'2110'
	,'2134'
	,'2203'
	,'2408'
	,'2419'
	,'2425'
	,'2395'
	,'2397'
	,'2477'
	,'2478'
	,'2483'
	,'2485'
	,'2469');

SELECT *
FROM user_reviews
WHERE CAST(SUBSTRING(product_id, 1, CHARINDEX('.', product_id)-1) AS int) IN ('1880'
	,'1973'
	,'1873'
	,'1866'
	,'1911'
	,'1860'
	,'1620'
	,'1800'
	,'1689'
	,'1742'
	,'1720'
	,'1767'
	,'2023'
	,'1921'
	,'1946'
	,'1962'
	,'2003'
	,'2026'
	,'2622'
	,'2738'
	,'2745'
	,'2800'
	,'2809');

DELETE FROM skincare_products
WHERE ingredients = 'No info';

UPDATE skincare_products
SET ingredients = REPLACE(ingredients, '''', '');

UPDATE skincare_products
SET ingredients = REPLACE(ingredients, '[', '');

UPDATE skincare_products
SET ingredients = REPLACE(ingredients, ']', '');

UPDATE skincare_products
SET ingredients = REPLACE(ingredients, '*', '');

SELECT ingredients, *
FROM skincare_products

SELECT *
FROM skincare_products
WHERE rating = 'nan';

SELECT *
FROM skincare_products
WHERE brand = '0';

UPDATE skincare_products
SET brand = 'Burt''s Bees'
WHERE brand = '0';

SELECT *
FROM skincare_products
WHERE product_url LIKE '%sephora.com%';

SELECT *
FROM sephora_ratings;

SELECT *
FROM skincare_products p
INNER JOIN cosmetics_rating s ON s.product_id = p.product_id;

UPDATE p
SET p.rating = s.Rank
FROM skincare_products p
INNER JOIN cosmetics_rating s
ON p.product_id = s.product_id;

UPDATE skincare_products
SET rating = CASE
	WHEN CAST(product_id AS int) BETWEEN 300 AND 500 THEN 5
	WHEN CAST(product_id AS int) BETWEEN 501 AND 700 THEN 4
	WHEN CAST(product_id AS int) BETWEEN 701 AND 1000 THEN 3
	WHEN CAST(product_id AS int) > 1000 THEN 2
	ELSE rating
	END
WHERE rating = 'nan';

WITH goodReviews AS (
	SELECT TRY_CAST(SUBSTRING(product_id, 1, CHARINDEX('.', product_id)-1) AS int) [id]
		,COUNT(stars) [Stars]
	FROM user_reviews
	WHERE stars <= 2
	GROUP BY product_id
)
UPDATE p
SET p.bad_reviews = r.Stars
FROM skincare_products p
INNER JOIN goodReviews r
ON p.product_id = r.id;

UPDATE skincare_products
SET bad_reviews = 0
WHERE bad_reviews = 'nan';

UPDATE skincare_products
SET good_reviews = 0
WHERE good_reviews = 'nan';

UPDATE skincare_products
SET description = 'Treat yourself to a unique experience. This product is bound to rejuvenate your senses. Simple and effective.'
WHERE description = 'Treat yourself to a unique experice. This product is bound to rejuvenate your senses. Simple and effective.';

UPDATE skincare_products
SET description = REPLACE(description, 'â€™', '''');

UPDATE skincare_products
SET description = REPLACE(description, 'Â', '');

UPDATE skincare_products
SET description = REPLACE(description, 'Ã', '');

SELECT *
FROM skincare_products
WHERE brand = 'nan';

SELECT *
FROM skincare_products
WHERE brand LIKE '%jo%';

UPDATE skincare_products
SET brand = 'Jo Malone London'
WHERE brand = 'nan'
	AND product_name LIKE 'Jo Malone%';

DELETE FROM skincare_products
WHERE brand = 'nan';

DELETE FROM user_reviews_sliced
WHERE TRY_CAST(SUBSTRING(product_id, 1, CHARINDEX('.', product_id)-1) AS int) NOT IN
(
	SELECT TRY_CAST(product_id AS int)
	FROM products_sliced
);

SELECT *
FROM skincare_products
WHERE product_name = 'nan';

SELECT *
FROM user_reviews
WHERE product_id = '2630.0';

DELETE FROM skincare_products
WHERE product_name = 'nan';

--SELECT * INTO products_sliced FROM skincare_products;

SELECT ISNULL(category, 'Brand Total') AS [Brand], COUNT(*) AS [Count]
FROM products_sliced
GROUP BY ROLLUP(category)
HAVING COUNT(*) > 20
ORDER BY COUNT(Brand) DESC;

DELETE FROM products_sliced
WHERE brand NOT IN
(
	SELECT ISNULL(brand, 'Brand Total') AS [Brand]
	FROM products_sliced
	GROUP BY ROLLUP(brand)
	HAVING COUNT(brand) > 20
);

SELECT *
FROM products_sliced
WHERE category = 'Bath Salts';

DROP TABLE products_sliced;

--SELECT * INTO user_reviews_sliced FROM user_reviews;

SELECT *
FROM user_reviews_sliced;

SELECT user_id, COUNT(*)
FROM user_reviews_multiple
GROUP BY user_id
HAVING COUNT(*) = 1;

SELECT product_id, COUNT(*)
FROM user_reviews_multiple
GROUP BY product_id
HAVING COUNT(*) = 1;

--SELECT * INTO user_reviews_multiple FROM user_reviews_sliced;

DELETE FROM user_reviews_multiple
WHERE product_id NOT IN
(
SELECT product_id
FROM user_reviews_multiple
GROUP BY product_id
HAVING COUNT(*) > 1
);

DROP TABLE user_reviews_multiple;