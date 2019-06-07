#!/usr/bin/env groovy
def call(String url){
    def get = new URL(url).openConnection();
    def getRC = get.getResponseCode();
    println(getRC);
}