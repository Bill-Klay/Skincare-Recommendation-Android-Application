import scrapy
import pandas as pd
from scrapy.selector import Selector

class WhiskeySpider(scrapy.Spider):
    name = 'skinCare'
    df = pd.read_excel("D:\My Projects\Skincare Recommendation\Data\links.xlsx")
    #count = 0
    #start_urls = ['https://www.lookfantastic.com/the-ordinary-natural-moisturising-factors-ha-30ml/11396687.html']
    start_urls = [f for f in df['product_url']]


    def parse(self, response):
        #try:
        #    yield {
        #        'product_name': response.css("h1.productName_title::text").get(),
        #        'description': response.css("div.productDescription_synopsisContent p::text").get(),
        #        'brand': response.css('div[data-information-component="brand"] div::text').get(),
        #        'rating': response.xpath("//a[@class='productReviewStars']//*[local-name()='svg']").attrib['aria-label'].replace(' Stars', ''),
        #        'image_url': response.xpath("//body[@id='health-beauty']/div/div/div/div/main[@id='mainContent']/div/div/div/div/div/div[1]/div[1]/img[1]").attrib['src'],
        #        'link': response.url
        #    }
        #except:
        #    yield {
        #        'description': 'description',
        #        'brand': 'brand',
        #        'rating': 'rating',
        #        'image_url': 'image_url',
        #        'link': response.url
        #    }
        for i in range(1, len(response.xpath("//div[@data-js-element='openModal']/div/div/h3").extract())):
            review = Selector(text=response.xpath("//div[@data-js-element='openModal']/div").extract()[i])
            try:
                yield {
                    'product_name': response.css("h1.productName_title::text").get(),
                    'title':  review.xpath('//div/h3/text()').get().replace("\n", ''),
                    'stars': review.xpath('//div/div/div/div/div').attrib['aria-label'].replace(" Stars ",''),
                    'review': review.xpath('//p/text()').get().replace("\n",''),
                    'username': review.xpath('//div/div/span[2]/text()').get()
                }
            except:
                yield{
                    'product_name': 'product_name',
                    'title': 'title',
                    'stars': 'stars',
                    'review': 'review',
                    'username': 'username'
                }

            #yield l.load_item()

        #count = count + 1
        #next_page = df['product_url'][count]
        #if count <= 1137:
        #    yield response.follow(next_page, callback=self.parse)