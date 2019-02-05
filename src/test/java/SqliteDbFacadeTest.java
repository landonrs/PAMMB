import db.SQLiteDbFacade;
import macro.Macro;
import macro.Step;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class SqliteDbFacadeTest {

    private SQLiteDbFacade sqliteDbFacade;

    @Before
    public void setUp() {
        sqliteDbFacade = SQLiteDbFacade.getInstance();
    }



    @Test
    public void shouldSaveAndLoadTestMacro() {
        Step testStep = new Step("letter", 56);
        ArrayList<Step> expectedSteps = new ArrayList<Step>(Arrays.asList(testStep));
        Macro testMacro = new Macro("test", expectedSteps, false);
        boolean result = sqliteDbFacade.saveMacro(testMacro);

        assertTrue(result);

        // now load macro
        Macro loadedMacro = sqliteDbFacade.loadMacro("test");


        assertNotNull(loadedMacro);
        assertEquals(loadedMacro.getSteps().size(), 1);
        assertEquals(loadedMacro.getName(), "test");
    }

}