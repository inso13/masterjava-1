package ru.javaops.masterjava.export;

import com.sun.jmx.remote.internal.ArrayQueue;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.ProjectDao;
import ru.javaops.masterjava.persist.model.City;
import ru.javaops.masterjava.persist.model.Group;
import ru.javaops.masterjava.persist.model.Project;
import ru.javaops.masterjava.xml.schema.GroupType;
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
 * Created by Котик on 03.04.2017.
 */

@Slf4j
public class ProjectExport {
    private ProjectDao projectDao = DBIProvider.getDao(ProjectDao.class);
    private static final int NUMBER_THREADS = 4;
    private ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_THREADS);

    @Value
    public static class FailedProjects extends Failed{
        public String nameOrRange;
        public String reason;

        @Override
        public String toString() {
            return nameOrRange + " : " + reason;
        }
    }

    public List<ProjectExport.FailedProjects> process(final InputStream is, int chunkSize) throws XMLStreamException {
        log.info("Start proseccing with chunkSize=" + chunkSize);

        return new Callable<List<ProjectExport.FailedProjects>>() {
            class ChunkFuture {
                String nameRange;
                Future<List<String>> future;

                public ChunkFuture(List<Project> chunk, Future<List<String>> future) {
                    this.future = future;
                    this.nameRange = chunk.get(0).getDescription();
                    if (chunk.size() > 1) {
                        this.nameRange += '-' + chunk.get(chunk.size() - 1).getDescription();
                    }
                }
            }

            @Override
            public List<ProjectExport.FailedProjects> call() throws XMLStreamException {
                List<ChunkFuture> futures = new ArrayList<>();

                int id = projectDao.getSeqAndSkip(chunkSize);
                List<Project> chunk = new ArrayList<>(chunkSize);
                final StaxStreamProcessor processor = new StaxStreamProcessor(is);

                while (processor.doUntil(XMLEvent.START_ELEMENT, "Project")) {
                    final String description = processor.getAttribute("name");
                    List<Group> groups = new ArrayList<>();
                    Project project = new Project(id++, description);

                    while (processor.doUntil(XMLEvent.START_ELEMENT, "Group"))
                    {
                        final String name = processor.getAttribute("name");
                        final String type = GroupType.valueOf(processor.getAttribute("type")).toString();
                        final Group group = new Group(project.getId(), name, type);
                        groups.add(group);
                        if (processor.getReader().next()==2) {break;} //TODO: breaks after each group
                    }
                    project.setGroups(groups);
                    chunk.add(project);
                    if (chunk.size() == chunkSize) {
                        futures.add(submit(chunk));
                        chunk.clear();
                        id = projectDao.getSeqAndSkip(chunkSize);
                    }
                }

                if (!chunk.isEmpty()) {
                    futures.add(submit(chunk));
                }

                List<ProjectExport.FailedProjects> failed = new ArrayList<>();
                futures.forEach(cf -> {
                    try {
                        failed.addAll(StreamEx.of(cf.future.get()).map(name -> new ProjectExport.FailedProjects(name, "already present")).toList());
                        log.info(cf.nameRange + " successfully executed");
                    } catch (Exception e) {
                        log.error(cf.nameRange + " failed", e);
                        failed.add(new ProjectExport.FailedProjects(cf.nameRange, e.toString()));
                    }
                });
                return failed;
            }

            private ChunkFuture submit(List<Project> chunk) {
                ChunkFuture chunkFuture = new ChunkFuture(chunk,
                        executorService.submit(() -> projectDao.insertAndGetAlreadyPresent(chunk))
                );
                log.info("Submit " + chunkFuture.nameRange);
                return chunkFuture;
            }
        }.call();
    }
}
