package com.analytics.transformers

import com.typesafe.config.Config
import org.codehaus.jettison.json.JSONObject

class AdvertiserTransformer extends Transfomer {

  override def transform(record: String, esConfigs:Config, jobConfigs: Config): String = {
    try {
      val winResponseJson = new JSONObject(record)
      val id = winResponseJson.getString("hash")
      val epochTime = winResponseJson.getLong("utc")

      // Get bidId, advertiserId, price, impression Id, campaign Id, campaign Type from Bid Response
      var indexName = jobConfigs.getString("search.bidresponse.index.name")
      val bidResponseAttrs = getRTBUtils().getDocumentAttributes(id, esConfigs, jobConfigs, indexName)
      //print("bidResponseAttrs"+ bidResponseAttrs)
      val responseBuffer = new JSONObject(bidResponseAttrs.getString("responseBuffer"))
      val seatBid = responseBuffer.getJSONArray("seatbid").getJSONObject(0)
      val bid = seatBid.getJSONArray("bid").getJSONObject(0)
      val advertiserId = seatBid.getString("seat")
      val price = bid.getDouble("price")
      val impId = bid.getString("impid")
      val bidId = bid.getString("id")
      val campaignId = bid.getString("cid")
      val campaignType = bid.getString("crid")

      // Get Impression Type from Bid Request
      indexName = jobConfigs.getString("search.bidrequest.index.name")
      val bidRequestAttrs = getRTBUtils().getDocumentAttributes(id, esConfigs, jobConfigs, indexName)
      //print("bidRequestAttrs", bidRequestAttrs)
      val impression = bidRequestAttrs.getJSONArray("imp").getJSONObject(0)
      var impressionType = ""
      if(!impression.isNull("banner"))
        impressionType = "Banner"
      else if(!impression.isNull("video"))
        impressionType = "Video"
      else if(!impression.isNull("native"))
        impressionType = "Native"

      // populate the advertiser JsonObject
      var advertiserJsonObj = new JSONObject()
      advertiserJsonObj.put("id", id)
      advertiserJsonObj.put("time", epochTime)
      advertiserJsonObj.put("advertiserId", advertiserId)
      advertiserJsonObj.put("expense", price)
      advertiserJsonObj.put("impression", impId)
      advertiserJsonObj.put("response", bidId)
      advertiserJsonObj.put("campaignId", campaignId)
      advertiserJsonObj.put("campaignType", campaignType)
      advertiserJsonObj.put("impressionType", impressionType)
      advertiserJsonObj.toString
    } catch {
      case ex:Exception => throw new Exception("Error during transform "+ex)
    }
  }
}
