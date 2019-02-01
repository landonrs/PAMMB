import Macro.Macro;

public interface DbFacade {

    Macro loadMacro(String macroName);

    boolean saveMacro(Macro userMacro);
}
