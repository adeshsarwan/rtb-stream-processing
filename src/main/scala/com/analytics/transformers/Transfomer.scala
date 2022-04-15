package com.analytics.transformers

import com.analytics.utils.RTBUtils
import com.typesafe.config.Config

trait Transfomer {
 var rtbUtils:RTBUtils = null

 def setRTBUtils(rtbUtils : RTBUtils): Unit ={
  this.rtbUtils = rtbUtils
 }

 def getRTBUtils():RTBUtils = {
  this.rtbUtils
 }
 def transform(record: String, esConfigs:Config, jobConfig: Config):String

}
