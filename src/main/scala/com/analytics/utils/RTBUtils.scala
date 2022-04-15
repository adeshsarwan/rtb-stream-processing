package com.analytics.utils

import java.util.Base64

import com.typesafe.config.Config
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import org.codehaus.jettison.json.JSONObject

class RTBUtils {

  def getDocumentAttributes(id:String, esEnvConfigs:Config, jobConfigs: Config, indexName: String): JSONObject = {
    try {
      val host = esEnvConfigs.getString("env.es.nodes")
      val port = esEnvConfigs.getString("env.es.port")
      var searchQuery = jobConfigs.getString(s"search.$indexName.query")
      searchQuery = searchQuery.replace("{id}", id)
      val url = host + ":" + port + "/" + indexName + "/" + searchQuery
      //println("Search url "+ url)
      val user = esEnvConfigs.getString("env.es.net.http.auth.user")
      val passwd = esEnvConfigs.getString("env.es.net.http.auth.pass")
      val token = user + ":" + passwd
      val post = new HttpPost(url)
      post.addHeader("Authorization", "Basic " + Base64.getUrlEncoder.encodeToString(token.getBytes))
      val response = (new DefaultHttpClient).execute(post)
      val content = scala.io.Source.fromInputStream(response.getEntity.getContent).mkString
      new JSONObject(content).getJSONObject("hits").getJSONArray("hits").getJSONObject(0).getJSONObject("_source")
    } catch {
      case ex:Exception => throw ex
    }
  }
}
