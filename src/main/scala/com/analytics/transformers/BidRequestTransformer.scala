package com.analytics.transformers

import com.typesafe.config.Config

class BidRequestTransformer extends Transfomer {
  override def transform(record: String, esConfigs:Config, jobConfig: Config): String = {
    record
  }

}
