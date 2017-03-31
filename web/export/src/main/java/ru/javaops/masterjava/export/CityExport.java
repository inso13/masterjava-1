package ru.javaops.masterjava.export;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.CityDao;
import ru.javaops.masterjava.persist.model.City;
import ru.javaops.masterjava.persist.model.User;
import ru.javaops.masterjava.persist.model.UserFlag;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * gkislin
 * 14.10.2016
 */
@Slf4j
public class CityExport {

    private CityDao cityDao = DBIProvider.getDao(CityDao.class);
    private static final int NUMBER_THREADS = 4;
    private ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_THREADS);

    @Value
    public static class FailedNames extends Failed{
        public String nameOrRange;
        public String reason;

        @Override
        public String toString() {
            return nameOrRange + " : " + reason;
        }
    }

    public List<FailedNames> process(final InputStream is, int chunkSize) throws XMLStreamException {
        log.info("Start proseccing with chunkSize=" + chunkSize);

        return new Callable<List<FailedNames>>() {
            class ChunkFuture {
                String nameRange;
                Future<List<String>> future;

                public ChunkFuture(List<City> chunk, Future<List<String>> future) {
                    this.future = future;
                    this.nameRange = chunk.get(0).getName();
                    if (chunk.size() > 1) {
                        this.nameRange += '-' + chunk.get(chunk.size() - 1).getName();
                    }
                }
            }

            @Override
            public List<FailedNames> call() throws XMLStreamException {
                List<ChunkFuture> futures = new ArrayList<>();

                int id = cityDao.getSeqAndSkip(chunkSize);
                List<City> chunk = new ArrayList<>(chunkSize);
                final StaxStreamProcessor processor = new StaxStreamProcessor(is);

                while (processor.doUntil(XMLEvent.START_ELEMENT, "City")) {
                    final String name = processor.getReader().getElementText();
                    final City city = new City(id++, name);
                    chunk.add(city);
                    if (chunk.size() == chunkSize) {
                        futures.add(submit(chunk));
                        chunk.clear();
                        id = cityDao.getSeqAndSkip(chunkSize);
                    }
                }

                if (!chunk.isEmpty()) {
                    futures.add(submit(chunk));
                }

                List<FailedNames> failed = new ArrayList<>();
                futures.forEach(cf -> {
                    try {
                        failed.addAll(StreamEx.of(cf.future.get()).map(name -> new FailedNames(name, "already present")).toList());
                        log.info(cf.nameRange + " successfully executed");
                    } catch (Exception e) {
                        log.error(cf.nameRange + " failed", e);
                        failed.add(new FailedNames(cf.nameRange, e.toString()));
                    }
                });
                return failed;
            }

            private ChunkFuture submit(List<City> chunk) {
                ChunkFuture chunkFuture = new ChunkFuture(chunk,
                        executorService.submit(() -> cityDao.insertAndGetAlreadyPresent(chunk))
                );
                log.info("Submit " + chunkFuture.nameRange);
                return chunkFuture;
            }
        }.call();
    }
}
