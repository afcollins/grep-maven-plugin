package net.radai.stubs;

import java.io.File;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.apache.maven.shared.utils.ReaderFactory;

public abstract class AbstractGrepMavenProjectStub extends MavenProjectStub {

    public AbstractGrepMavenProjectStub() {
        MavenXpp3Reader pomReader = new MavenXpp3Reader();
        Model model;

        try {
            model = pomReader.read(ReaderFactory.newXmlReader(new File(getBasedir(), "pom.xml")));
            setModel(model);

            setGroupId(model.getGroupId());
            setArtifactId(model.getArtifactId());
            setVersion(model.getVersion());
            setName(model.getName());
            setPackaging(model.getPackaging());

            Build build = new Build();
            build.setDirectory(getBasedir() + "/target");
            setBuild(build);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Relative to the typical path
     * 
     * @return
     */
    protected abstract String getUnitTestProjectDirectory();

    @Override
    public File getBasedir() {
        return new File(super.getBasedir(), "/target/test-classes/unit/" + getUnitTestProjectDirectory());
    }
}
