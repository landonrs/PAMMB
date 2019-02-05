package db;

import macro.Macro;

public interface DbFacade {

    Macro loadMacro(String macroName);

    boolean saveMacro(Macro userMacro);
}
