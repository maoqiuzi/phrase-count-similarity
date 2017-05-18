# phrase-count-similarity

## Installation
Change the elasticsearch.version in pom into target elasticsearch version. Tested in 5.3.1 and 5.4.0.

Run `mvn package` to compile project

Then run
`/bin/elasticsearch-plugins install file:/path-to-file/filename.zip`

Make sure to include the "file:/" inside the cmd, because otherwise elasticsearch would return unknown plugin.

Or download the zip file from target/releases/

## Usage
sample usage in Kibana:
```
PUT my_index
 {
   "similarity": {
       "phrase-count-similarity": {
         "type": "phrase-count-similarity"
       }
     }
 }
 
 PUT my_index/_mapping/my_type
 {
   "properties": {
     "body": {
       "type": "text",
       "similarity":  "phrase-count-similarity"
     }
   }
 }
 
 PUT my_index/my_type/1
 {
   "body": "A A B"
 }
 
 GET my_index/_search
 {
   "explain": true,
   "size": 5,
     "query": {
       "bool": {
         "must": {
           "match_phrase": {
             "body": {
               "query": "A B",
               "slop": 2
             }
           }
         }
       }
     }
 }
```
