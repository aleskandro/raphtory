import com.typesafe.sbt.packager.archetypes.scripts.AshScriptPlugin


	val Akka            = "2.5.2"
	val Config          = "1.2.1"
	val JodaT           = "2.3"
	val Logback         = "1.1.2"
	val Scala           = "2.12.4"
	val Slf4j           = "1.7.7"
	//val ScalaJack		    = "4.0"
	val SbtPackager 	  = "1.2.0"


	val resolutionRepos = Seq(
		"Typesafe Repo" 	at "http://repo.typesafe.com/typesafe/releases/",
		"Akka Snapshots"	at "http://repo.akka.io/snapshots/",
		"OSS"				at "http://oss.sonatype.org/content/repositories/releases",
		"Mvn" 				at "http://mvnrepository.com/artifact"  // for commons_exec
	)

	def dep_compile   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "compile")
	def dep_test      (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "test")  // test vs test so as not to confuse w/sbt 'test'

	val akka_actor		  = "com.typesafe.akka"		%% "akka-actor"		      % Akka
	val akka_slf4j 		  = "com.typesafe.akka" 	%% "akka-slf4j"		      % Akka
	val akka_remote		  = "com.typesafe.akka" 	%% "akka-remote"	      % Akka
	val akka_cluster	  = "com.typesafe.akka" 	%% "akka-cluster" 	    % Akka
	val akka_contrib	  = "com.typesafe.akka" 	%% "akka-contrib" 	    % Akka
	val akka_tools 		  = "com.typesafe.akka"		%% "akka-cluster-tools" % Akka
  val akka_streams    = "com.typesafe.akka"   %  "akka-stream_2.12"   % "2.5.2"
  val akka_http       = "com.typesafe.akka"   % "akka-http_2.12"      % "10.0.7"
	val typesafe_config	= "com.typesafe"			  %  "config"			        % Config
	val spray_json      = "io.spray"            % "spray-json_2.12"     % "1.3.3"
	//val scalajack		    = "co.blocke"           % "scalajack_2.12"      % "4.1"
	val logback			    = "ch.qos.logback" 			% "logback-classic"	    % Logback
	val slf4j_simple 	  = "org.slf4j" 				  % "slf4j-simple" 	      % Slf4j

	//Zookeeper tings
	val curator1        = "org.apache.curator"  % "curator-framework"   % "2.12.0"
	val curator2        = "org.apache.curator"  % "curator-recipes"     % "2.12.0"

	val kamon						= "io.kamon"					 %% "kamon-core"					% "1.1.0"
	val kamon_prometheus= "io.kamon"					 %% "kamon-prometheus"		% "1.0.0"
  val kamon_akka      = "io.kamon"					 %% "kamon-akka-2.5"		  % "1.0.1"
	val kamon_system 		= "io.kamon" 					 %% "kamon-system-metrics"% "1.0.0"
	val kamon_repos     = Seq(Resolver.bintrayRepo("kamon-io", "snapshots"))


	val IP = java.net.InetAddress.getLocalHost().getHostAddress()

	lazy val basicSettings = Seq(
 		organization 				:= "com.gwz",
		description 				:= "Raphtory Distributed Graph Stream Processing",
		startYear 					:= Some(2014),
		scalaVersion 				:= Scala,
		packageName         := "raphtory",
		parallelExecution in Test 	:= false,
		//resolvers					++= Dependencies.resolutionRepos,
		resolvers					  ++= kamon_repos,
		scalacOptions				:= Seq("-feature", "-deprecation", "-encoding", "UTF8", "-unchecked"),
		testOptions in Test += Tests.Argument("-oDF"),
		version 					:= "latest"
	)

	lazy val dockerStuff = Seq(
		maintainer := "Ben Steer <b.a.steer@qmul.ac.uk>",
		dockerBaseImage := "miratepuffin/raphtory-redis",
        dockerRepository := Some("miratepuffin"),
		dockerExposedPorts := Seq(2551,8080,2552) ++ (9000 to 20000)
		)

	lazy val root = Project(id = "raphtory",
		base = file(".")) aggregate(cluster)

	lazy val cluster = project.in(file("cluster"))
		.enablePlugins(JavaAppPackaging)
		.enablePlugins(AshScriptPlugin)
	  	.enablePlugins(JavaAgent)
		.settings(isSnapshot := true)
		.settings(dockerStuff:_*)
		.settings(dockerEntrypoint := Seq("/opt/docker/bin/cluster"))
		.settings(basicSettings: _*)
		.settings(libraryDependencies ++=
			dep_compile(
				typesafe_config, akka_http, akka_streams, akka_actor, akka_cluster, akka_tools,
				akka_contrib, akka_remote, akka_slf4j, logback,spray_json,curator1,curator2,
				kamon, kamon_akka, kamon_prometheus, kamon_system)
		)
	  	.settings(
          javaAgents += "org.aspectj" % "aspectjweaver" % "1.8.13",
          javaOptions in Universal += "-Dorg.aspectj.tracing.factory=default"
			)
		//.settings((Keys.test in Test) <<= (Keys.test in Test) dependsOn (publishLocal in Docker))
