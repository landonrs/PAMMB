package speechHandling;

import frontEnd.AssistantModeController;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.junit.Assert.assertEquals;

public class SpeechCommandHandlerTest {

    @Mock
    private SpeechInterpreter interpreterMock;

    @Mock
    AssistantModeController mockController;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private SpeechCommandHandler handler;

    @Before
    public void setUp() throws Exception {
        handler = SpeechCommandHandler.getInstance();
    }



    @Test
    public void shouldSetStateToActivated() {
        handler.handleAssistantCommand("listen up pam", mockController);

        assertEquals(SpeechCommandHandler.ACTIVE_STATE.ACTIVATED, handler.getCurrentState());
    }

    @Test
    public void shouldSetStateToIdleAfterActivation() {
        // first set to activated
        handler.handleAssistantCommand("listen up pam", mockController);
        assertEquals(SpeechCommandHandler.ACTIVE_STATE.ACTIVATED, handler.getCurrentState());
        // now set to idle
        handler.handleAssistantCommand("stop listening", mockController);
        assertEquals(SpeechCommandHandler.ACTIVE_STATE.IDLE, handler.getCurrentState());
    }


    @Test
    public void shouldTrimCommandFromInput() {
        String command = handler.getCommandFromSpeech("please run command hello world");

        assertEquals("hello world", command);
    }



}