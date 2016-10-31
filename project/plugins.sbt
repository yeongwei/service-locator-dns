logLevel := Level.Warn

resolvers += Resolver.typesafeRepo("releases")
// resolvers += "spray repo" at "http://nightlies.spray.io"

addSbtPlugin("com.typesafe.sbt"  % "sbt-scalariform" % "1.3.0")
addSbtPlugin("de.heikoseeberger" % "sbt-header"      % "1.5.1")
addSbtPlugin("com.jsuereth"      % "sbt-pgp"         % "1.0.0")
addSbtPlugin("com.github.gseitz" % "sbt-release"     % "1.0.3")
addSbtPlugin("org.xerial.sbt"    % "sbt-sonatype"    % "1.1")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "4.0.0")
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")
