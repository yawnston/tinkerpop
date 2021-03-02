/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.tinkerpop.gremlin.server.GremlinServer
import org.apache.tinkerpop.gremlin.server.Settings

////////////////////////////////////////////////////////////////////////////////
// IMPORTANT
////////////////////////////////////////////////////////////////////////////////
// Changes to this file need to be appropriately replicated to
//
// - docker/gremlin-server/gremlin-server-integration.yaml
// - docker/gremlin-server/gremlin-server-integration-secure.yaml
//
// Without such changes, the test docker server can't be started for independent
// testing with Gremlin Language Variants.
////////////////////////////////////////////////////////////////////////////////

if (Boolean.parseBoolean(skipTests)) return

log.info("Starting Gremlin Server instances for native testing of ${executionName}")
def settings = Settings.read("${settingsFile}")
settings.graphs.graph = gremlinServerDir + "/src/test/scripts/tinkergraph-empty.properties"
settings.graphs.classic = gremlinServerDir + "/src/test/scripts/tinkergraph-empty.properties"
settings.graphs.modern = gremlinServerDir + "/src/test/scripts/tinkergraph-empty.properties"
settings.graphs.crew = gremlinServerDir + "/src/test/scripts/tinkergraph-empty.properties"
settings.graphs.grateful = gremlinServerDir + "/src/test/scripts/tinkergraph-empty.properties"
settings.graphs.sink = gremlinServerDir + "/src/test/scripts/tinkergraph-empty.properties"
settings.scriptEngines["gremlin-groovy"].plugins["org.apache.tinkerpop.gremlin.jsr223.ScriptFileGremlinPlugin"].files = [gremlinServerDir + "/src/test/scripts/generate-all.groovy"]
if (Boolean.parseBoolean(python)) {
    settings.scriptEngines["gremlin-python"] = new Settings.ScriptEngineSettings()
    settings.scriptEngines["gremlin-jython"] = new Settings.ScriptEngineSettings()
}
settings.port = 45940

def server = new GremlinServer(settings)
server.start().join()

project.setContextValue("gremlin.server", server)
log.info("Gremlin Server with no authentication started on port 45940")

def securePropsFile = new File("${projectBaseDir}/target/tinkergraph-credentials.properties")
if (!securePropsFile.exists()) {
    securePropsFile.createNewFile()
    securePropsFile << "gremlin.graph=org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph\n"
    securePropsFile << "gremlin.tinkergraph.vertexIdManager=LONG\n"
    securePropsFile << "gremlin.tinkergraph.graphLocation=${gremlinServerDir}/data/credentials.kryo\n"
    securePropsFile << "gremlin.tinkergraph.graphFormat=gryo"
}

def settingsSecure = Settings.read("${settingsFile}")
settingsSecure.graphs.graph = gremlinServerDir + "/src/test/scripts/tinkergraph-empty.properties"
settingsSecure.graphs.classic = gremlinServerDir + "/src/test/scripts/tinkergraph-empty.properties"
settingsSecure.graphs.modern = gremlinServerDir + "/src/test/scripts/tinkergraph-empty.properties"
settingsSecure.graphs.crew = gremlinServerDir + "/src/test/scripts/tinkergraph-empty.properties"
settingsSecure.graphs.grateful = gremlinServerDir + "/src/test/scripts/tinkergraph-empty.properties"
settingsSecure.graphs.sink = gremlinServerDir + "/src/test/scripts/tinkergraph-empty.properties"
settingsSecure.scriptEngines["gremlin-groovy"].plugins["org.apache.tinkerpop.gremlin.jsr223.ScriptFileGremlinPlugin"].files = [gremlinServerDir + "/src/test/scripts/generate-all.groovy"]
if (Boolean.parseBoolean(python)) {
    settingsSecure.scriptEngines["gremlin-python"] = new Settings.ScriptEngineSettings()
    settingsSecure.scriptEngines["gremlin-jython"] = new Settings.ScriptEngineSettings()
}
settingsSecure.port = 45941
settingsSecure.authentication.authenticator = "org.apache.tinkerpop.gremlin.server.auth.SimpleAuthenticator"
settingsSecure.authentication.config = [credentialsDb: projectBaseDir + "/target/tinkergraph-credentials.properties"]
settingsSecure.ssl = new Settings.SslSettings()
settingsSecure.ssl.enabled = true
settingsSecure.ssl.sslEnabledProtocols = ["TLSv1.2"]
settingsSecure.ssl.keyStore = gremlinServerDir + "/src/test/resources/server-key.jks"
settingsSecure.ssl.keyStorePassword = "changeit"

def serverSecure = new GremlinServer(settingsSecure)
serverSecure.start().join()

project.setContextValue("gremlin.server.secure", serverSecure)
log.info("Gremlin Server with authentication started on port 45941")