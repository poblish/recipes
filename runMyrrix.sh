pkill -f net.myrrix.web.Runner
pkill -f org.elasticsearch.bootstrap.ElasticSearch

nohup java -Dmodel.features=11 -Dmodel.als.lambda=0.66 -cp "/Users/andrewregan/Development/java/recipe_explorer/myrrix-serving-1.0.1.jar:/Users/andrewregan/.m2/repository/uk/co/recipes/myrrix-rescorer/0.0.1-SNAPSHOT/myrrix-rescorer-0.0.1-SNAPSHOT.jar" net.myrrix.web.Runner --port 8080 --rescorerProviderClass=uk.co.recipes.myrrix.RecipesRescorer > /private/tmp/nohup.out 2>&1&
nohup elasticsearch -f -D es.config=/usr/local/Cellar/elasticsearch/0.90.2/config/elasticsearch.yml     > /private/tmp/nohup.out 2>&1&
