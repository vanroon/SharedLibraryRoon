import org.junit.*
import com.lesfurets.jenkins.unit.*
import static groovy.test.GroovyAssert.*

class ExtractFile extends BasePipelineTest {
    def extractFile
    
    @Before
    void setUp() {
        super.setUp()

        extractFile = loadScript("vars/extractFile.groovy")
    }

    @Test
    void testCall() {
        def numEntries = extractFile("resources/exampleFiles/example.zip")
        //assert 2, numEntries
    }
}