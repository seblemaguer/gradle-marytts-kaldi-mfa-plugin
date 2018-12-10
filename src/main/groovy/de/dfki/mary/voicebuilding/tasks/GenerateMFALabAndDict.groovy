package de.dfki.mary.voicebuilding.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*

// Configuration
import marytts.config.MaryConfiguration
import marytts.config.JSONMaryConfigLoader

// Runtime / Request
import marytts.runutils.Mary
import marytts.runutils.Request


class GenerateMFALabAndDict extends DefaultTask {

    // FIXME: move as a resource
    @Internal
    String configuration = ""

    @InputDirectory
    final DirectoryProperty srcDir = newInputDirectory()

    @OutputDirectory
    final DirectoryProperty destDir = newOutputDirectory()

    @OutputFile
    final RegularFileProperty dictFile = newOutputFile()

    @TaskAction
    void convert() {
        // Start Mary
        Mary.startup();

        // Call Mary
        def dict = [:]
        project.fileTree(srcDir).include('*.txt').collect { txtFile ->
            try {
                // Read the text
                String input_data = txtFile.text;

                // Read the configuration
                InputStream configuration_stream = this.class.getResourceAsStream("/en_US_default.json")
	        MaryConfiguration conf_object = (new JSONMaryConfigLoader()).loadConfiguration(configuration_stream);

                // Call mary && Get the output utterance
	        def request = new Request(conf_object, input_data);
                request.process();
                def output = request.serializeFinaleUtterance();

                // Fill the dict
                output["phonetisation"].each { k,v ->
                    dict[k] = v
                }

                // Get the tokens and save the MFA lab
                destDir.file(txtFile.name - '.txt' + '.lab').get().asFile.withWriter('UTF-8') { out ->
                    out.println output["tokens"]
                }
            } catch (Exception ex) {
                project.logger.error "Excluding $txtFile.name : ${ex}"
                // FIXME: more detail message
            }
        }

        // Save dictionnary
        dictFile.get().asFile.withWriter('UTF-8') { out ->
            dict.toSorted { it.key.toString()toLowerCase() }.each { word, phonemes ->
                out.println "$word $phonemes"
            }
        }

        // Shutdown mary
        Mary.shutdown()
    }
}
