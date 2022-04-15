package com.analytics.transformers

import com.typesafe.config.Config
import org.codehaus.jettison.json.JSONObject

class PublisherTransformer extends Transfomer {

  override def transform(record: String, esConfigs:Config, jobConfigs: Config): String = {
    try {
      val winResponseJson = new JSONObject(record)
      val id = winResponseJson.getString("hash")
      val epochTime = winResponseJson.getLong("utc")

      // Get price from Bid Response
      var indexName = jobConfigs.getString("search.bidresponse.index.name")
      val bidResponseAttrs = getRTBUtils().getDocumentAttributes(id, esConfigs, jobConfigs, indexName)
      //print("bidResponseAttrs"+ bidResponseAttrs)
      val responseBuffer = new JSONObject(bidResponseAttrs.getString("responseBuffer"))
      val price = responseBuffer.getJSONArray("seatbid").getJSONObject(0).
        getJSONArray("bid").getJSONObject(0).getDouble("price")

      // Get publisher, site, impression, request from bidrequest
      indexName = jobConfigs.getString("search.bidrequest.index.name")
      val bidRequestAttrs = getRTBUtils().getDocumentAttributes(id, esConfigs, jobConfigs, indexName)
      //print("bidRequestAttrs", bidRequestAttrs)
      val bidId = bidRequestAttrs.getString("id")
      val impression = bidRequestAttrs.getJSONArray("imp").getJSONObject(0)
      val impId = impression.getString("id")
      var impressionType = ""
      if(!impression.isNull("banner"))
        impressionType = "Banner"
      else if(!impression.isNull("video"))
        impressionType = "Video"
      else if(!impression.isNull("native"))
        impressionType = "Native"

      val site = bidRequestAttrs.getJSONObject("site")
      val siteId = site.getString("id")
      val siteName = site.getString("name")
      val publisherId = site.getJSONObject("publisher").getString("id")
      val publisherName = site.getJSONObject("publisher").getString("name")

      // populate the publisher JsonObject
      var publisherJsonObj = new JSONObject()
      publisherJsonObj.put("id", id)
      publisherJsonObj.put("revenue", price)
      publisherJsonObj.put("request", bidId)
      publisherJsonObj.put("impression", impId)
      publisherJsonObj.put("impressionType", impressionType)
      publisherJsonObj.put("siteId", siteId)
      publisherJsonObj.put("siteName", siteName)
      publisherJsonObj.put("publisherId", publisherId)
      publisherJsonObj.put("publisherName", publisherName)
      publisherJsonObj.put("time", epochTime)

      publisherJsonObj.toString
    } catch {
      case ex:Exception => throw new Exception("Error during transform "+ex)
    }
  }
}
