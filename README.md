# phrase-count-similarity

## Installation
run `mvn package` to compile project

then run
`/bin/elasticsearch-plugins install file:/path-to-file/filename.zip`

make sure to include the "file:/" inside the cmd, because otherwise elasticsearch would return unknown plugin.

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
