/*
 * Copyright 2020 Hazelcast Inc.
 *
 * Licensed under the Hazelcast Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://hazelcast.com/hazelcast-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.hazelcast.commandline.managementcenter;

import com.hazelcast.commandline.AbstractCommandLine;
import com.hazelcast.commandline.HazelcastVersionProvider;
import com.hazelcast.commandline.ProcessExecutor;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static com.hazelcast.commandline.HazelcastCommandLine.SEPARATOR;
import static com.hazelcast.internal.util.StringUtil.isNullOrEmpty;

/**
 * Commandline class for Hazelcast Management Center operations
 */
@CommandLine.Command(name = "mc", description = "Utility for Hazelcast Management Center operations.",
        versionProvider = HazelcastVersionProvider.class, mixinStandardHelpOptions = true, sortOptions = false)
public class ManagementCenterCommandLine extends AbstractCommandLine {

    private String mcVersion;

    public ManagementCenterCommandLine(PrintStream out, PrintStream err, ProcessExecutor processExecutor)
            throws IOException {
        super(out, err, processExecutor, false);
        mcVersion = new HazelcastVersionProvider().getMcVersion();
    }

    @Override
    public void run() {
        List<CommandLine> parsed = spec.commandLine().getParseResult().asCommandLineList();
        if (parsed != null && parsed.size() == 1) {
            spec.commandLine().usage(out);
        }
    }

    @CommandLine.Command(description = "Starts a new Hazelcast Management Center instance", mixinStandardHelpOptions = true)
    public void start(
            @CommandLine.Option(names = {"-c", "--context-path"}, paramLabel = "<context-path>",
                    description = "Bind to the specified <context-path>.")
                    String contextPath,
            @CommandLine.Option(names = {"-p", "--port"}, paramLabel = "<port>",
                    description = "Bind to the specified <port>.", defaultValue = "8080")
                    String port,
            @CommandLine.Option(names = {"-J", "--JAVA_OPTS"}, paramLabel = "<option>", parameterConsumer = JavaOptionsConsumer.class,
                    description = "Specify additional Java <option> (Use ',' to separate multiple options).")
                    List<String> javaOptions,
            @CommandLine.Option(names = {"-v", "--verbose"},
                    description = "Output with FINE level verbose logging.")
            boolean verbose,
            @CommandLine.Option(names = {"-vv", "--vverbose"},
                    description = "Output with FINEST level verbose logging.")
            boolean finestVerbose)
            throws IOException, InterruptedException {
        List<String> args = new ArrayList<>();
        if (!isNullOrEmpty(contextPath)) {
            args.add("-Dhazelcast.mc.contextPath=" + contextPath);
        }
        args.add("-Dhazelcast.mc.http.port=" + port);
        if (javaOptions != null && javaOptions.size() > 0) {
            args.addAll(javaOptions);
        }
        addLogging(args, verbose, finestVerbose);

        buildAndStartManagementCenter(args);
    }

    private void buildAndStartManagementCenter(List<String> parameters)
            throws IOException, InterruptedException {
        List<String> commandList = new ArrayList<>();
        String path = System.getProperty("java.home") + SEPARATOR + "bin" + SEPARATOR + "java";
        commandList.add(path);
        commandList.add("-cp");
        commandList.add(WORKING_DIRECTORY + "/artifacts/hazelcast-management-center-" + mcVersion + ".war");
        commandList.addAll(parameters);
        commandList.add("com.hazelcast.webmonitor.Launcher");
        processExecutor.buildAndStart(commandList);
    }

}