package org.kendar.replayer.storage;

import org.kendar.events.EventQueue;
import org.kendar.replayer.ReplayerState;
import org.kendar.replayer.events.NullCompleted;
import org.kendar.replayer.utils.JsReplayerExecutor;
import org.kendar.replayer.utils.Md5Tester;
import org.kendar.servers.http.InternalRequester;
import org.kendar.servers.http.Response;
import org.kendar.utils.LoggerBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;


public class NullDataset extends ReplayerDataset{
    private EventQueue eventQueue;
    private InternalRequester internalRequester;
    private Thread thread;
    private String id;
    private AtomicBoolean running = new AtomicBoolean(false);
    private JsReplayerExecutor executor = new JsReplayerExecutor();

    public NullDataset(
            LoggerBuilder loggerBuilder,
            DataReorganizer dataReorganizer,
            Md5Tester md5Tester, EventQueue eventQueue, InternalRequester internalRequester) {
        super(loggerBuilder,dataReorganizer,md5Tester);
        this.eventQueue = eventQueue;
        this.internalRequester = internalRequester;
    }


    @Override
    public ReplayerState getType() {
        return ReplayerState.PLAYING_NULL_INFRASTRUCTURE;
    }

    @Override
    public String getName() {
        return name;
    }

    public String start() {
        id = UUID.randomUUID().toString();
        thread = new Thread(()-> {
            try {
                runNullDataset(id);
            } catch (Exception e) {
                logger.error("ERROR EXECUTING RECORDING",e);
            }
        });
        thread.start();
        return id;
    }

    @Override
    protected boolean superMatch(ReplayerRow row) {
        return row.isStimulatedTest();
    }

    private void runNullDataset(String id) throws IOException {
        running.set(true);
        var result = new TestResults();
        result.setType("NullInfrastructure");
        result.setTimestamp(Calendar.getInstance());
        long start = System.currentTimeMillis();
        var rootPath = Path.of(replayerDataDir);

        var stringPath = Path.of(rootPath + File.separator + name + ".json");
        var nullDir = Path.of(rootPath + File.separator + "null" + File.separator);
        var resultsFile =  Path.of(rootPath + File.separator + "null" + File.separator+ id+".json");
        try {
            if (!Files.isDirectory(rootPath)) {
                Files.createDirectory(rootPath);
            }
            if (!Files.exists(nullDir)) {
                Files.createDirectory(nullDir);
            }
            var maps = new HashMap<Integer, ReplayerRow>();
            var replayerResult = mapper.readValue(stringPath.toFile(), ReplayerResult.class);
            for (var call : replayerResult.getStaticRequests()) {
                maps.put(call.getId(), call);
            }
            for (var call : replayerResult.getDynamicRequests()) {
                maps.put(call.getId(), call);
            }
            var indexes = replayerResult.getIndexes().stream()
                    .filter(a -> a.isStimulatorTest())
                    .sorted(Comparator.comparingInt(CallIndex::getId))
                    .collect(Collectors.toList());
            try {
                for (var toCall : indexes) {
                    if (!running.get()) break;
                    var reqResp = maps.get(toCall.getReference());
                    var response = new Response();
                    internalRequester.callSite(reqResp.getRequest(), response);
                    var script = executor.prepare(toCall.getJsCallback());
                    executor.run(reqResp.getRequest(), response, reqResp.getResponse(), script);
                    result.getExecuted().add(toCall.getId());
                }
            } catch (Exception ex) {
                result.setError(ex.getMessage());
            }
        }catch (Exception e){
            result.setError(e.getMessage());
        }
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        result.setDuration(timeElapsed);
        var toWrite = mapper.writeValueAsString(result);
        Files.writeString(resultsFile,toWrite);
        this.eventQueue.handle(new NullCompleted());
    }
    public void stop() {
        running.set(false);
    }
}
