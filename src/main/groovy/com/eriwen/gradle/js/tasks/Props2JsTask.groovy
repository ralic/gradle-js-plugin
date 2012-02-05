/**
 * Copyright 2012 Eric Wendelin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.eriwen.gradle.js.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import com.eriwen.gradle.js.ResourceUtil

class Props2JsTask extends DefaultTask {
    private static final String PROPS2JS_JAR = 'props2js-0.1.0.jar'
    private static final String TMP_DIR = 'tmp/js'
    private static final ResourceUtil RESOURCE_UTIL = new ResourceUtil()
    private static final Set<String> AVAILABLE_TYPES = ['js', 'json', 'jsonp']

    String functionName = ''
    String type = 'json'

    @TaskAction
    def run() {
        def inputFiles = getInputs().files.files.toArray()
        def outputFiles = getOutputs().files.files.toArray()

        // Prevent arguments that don't make sense
        if (!AVAILABLE_TYPES.contains(type)) {
            throw new IllegalArgumentException("Invalid type specified. Must be one of: ${AVAILABLE_TYPES.join(',')}")
        } else if (type == 'json' && functionName) {
            throw new IllegalArgumentException("Cannot specify a 'functionName' when type is 'json'")
        } else if (type != 'json' && !functionName) {
            throw new IllegalArgumentException("Must specify a 'functionName' when type is 'jsonp' or 'js'")
        }

        if (outputFiles.size() == 1 && inputFiles.size() == 1) {
            final File props2JsJar = RESOURCE_UTIL.extractFileToDirectory(new File(project.buildDir, TMP_DIR), PROPS2JS_JAR)
            // Equivalent to java -jar tmp/js/props2js-0.1.0.jar [inputFile] -t [type] (--name [functionName]) -o [outputFile]
            ant.java(jar: props2JsJar.canonicalPath, fork: true) {
                arg(value: (inputFiles[0] as File).canonicalPath)
                arg(value: '-t')
                arg(value: type)
                if (functionName) {
                    arg(value: '--name')
                    arg(value: functionName)
                }
                arg(value: '-o')
                arg(value: outputFiles[0])
            }
        } else {
            throw new IllegalArgumentException("Could not map input files to output files. Found ${inputFiles.size()} inputs and ${outputFiles.size()} outputs")
        }
    }
}