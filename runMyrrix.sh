#!/usr/bin/env bash
pkill -f net.myrrix.web.Runner
pkill -f org.elasticsearch.bootstrap.ElasticSearch

mvn clean install -DskipTests=true
mvn -f myrrixRescorer/ clean install  -DskipTests=true

cp recipes_config.yml ~/recipes_config.yml

nohup ~/Desktop/elasticsearch-5.5.1/bin/elasticsearch &
# Was: docker run --rm --name es -d -p 9200:9200 -p 9300:9300 -e "http.host=0.0.0.0" -e "transport.host=0.0.0.0" -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" -e "xpack.security.enabled=false" docker.elastic.co/elasticsearch/elasticsearch:5.5.1
# Was: nohup  ~/Desktop/elasticsearch-0.90.6/bin/elasticsearch &

nohup java -Dmodel.features=6 -Dmodel.als.lambda=1.03 -cp "/Users/andrewregan/Development/java/recipe_explorer/myrrix-serving-1.0.1.jar:/Users/andrewregan/.m2/repository/uk/co/recipes/myrrix-rescorer/0.0.1-SNAPSHOT/myrrix-rescorer-0.0.1-SNAPSHOT.jar" net.myrrix.web.Runner --port 8080 --rescorerProviderClass=uk.co.recipes.myrrix.RecipesRescorer > /private/tmp/nohup.out 2>&1&
nohup java -Dmodel.features=6 -Dmodel.als.lambda=1.03 -cp "./myrrix-serving-1.0.1.jar:./myrrix-rescorer-0.0.1-SNAPSHOT.jar" net.myrrix.web.Runner --port 8080 --rescorerProviderClass=uk.co.recipes.myrrix.RecipesRescorer > /tmp/nohup.out 2>&1&
# nohup elasticsearch -f -D es.config=/usr/local/Cellar/elasticsearch/0.90.6/config/elasticsearch.yml     > /private/tmp/nohup.out 2>&1&


# cd web/recipes-web
# ../../play-2.1.5/play .
