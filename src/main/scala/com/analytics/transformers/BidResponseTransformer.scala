package com.analytics.transformers

import com.typesafe.config.Config
import org.codehaus.jettison.json.JSONObject

class BidResponseTransformer extends Transfomer {
  override def transform(record: String, esConfigs:Config, jobConfig: Config): String = {
    var jsonObject =  new JSONObject(record)
    jsonObject.put("responseBuffer", new JSONObject(jsonObject.getString("responseBuffer")))
    jsonObject.toString
  }
}
