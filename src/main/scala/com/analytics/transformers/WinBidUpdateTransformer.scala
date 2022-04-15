package com.analytics.transformers

import com.typesafe.config.Config
import org.codehaus.jettison.json.JSONObject

class WinBidUpdateTransformer extends Transfomer {
  override def transform(record: String, esConfigs:Config, jobConfig: Config): String = {
    var jsonObject =  new JSONObject(record)
    var bidUpdateObject = new JSONObject()
    bidUpdateObject.put("bidid", jsonObject.getString("hash"))
    bidUpdateObject.put("bidwin", "Y")
    bidUpdateObject.toString
  }
}
