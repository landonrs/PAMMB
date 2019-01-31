package speechHandling;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

public class SpeechCommandHandlerTest {

    @Mock
    private SpeechInterpreter interpreterMock;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private SpeechCommandHandler handler;

    @Before
    public void setUp() throws Exception {
        handler = SpeechCommandHandler.getInstance();
    }



    @Test
    public void shouldSetStateToActivated() {
        handler.handleCommand("hey pam");

        assertEquals(SpeechCommandHandler.ACTIVE_STATE.ACTIVATED, handler.getCurrentState());
    }

    @Test
    public void shouldSetStateToIdleAfterActivation() {
        // first set to activated
        handler.handleCommand("hey pam");
        assertEquals(SpeechCommandHandler.ACTIVE_STATE.ACTIVATED, handler.getCurrentState());
        // now set to idle
        handler.handleCommand("stop listening");
        assertEquals(SpeechCommandHandler.ACTIVE_STATE.IDLE, handler.getCurrentState());
    }


}