package android.tools;

import android.tools.command.ActivityCommand;
import android.tools.command.Command;
import android.tools.command.CommonCommand;
import android.tools.command.DebugCommand;
import android.tools.command.PackageCommand;
import android.tools.command.ServiceCommand;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import org.ironman.framework.util.LogUtil;

public class Main {

    private static final String TAG = "android-tools";
    private static final String FLAG_BEGIN = " -*- output -*- by -*- android -*- tools -*- begin -*- ";
    private static final String FLAG_END = " -*- output -*- by -*- android -*- tools -*- end -*- ";

    @Parameter(names = "--add-flag", hidden = true)
    private boolean flag = false;

    private static void parseArgs(String[] args) throws Throwable {
        Main main = new Main();

        JCommander.Builder builder = JCommander.newBuilder().addObject(main);
        builder.addCommand(new CommonCommand());
        builder.addCommand(new PackageCommand());
        builder.addCommand(new ActivityCommand());
        builder.addCommand(new ServiceCommand());
        builder.addCommand(new DebugCommand());

        JCommander commander = builder.build();
        commander.parse(args);

        int index = 0;
        if (main.flag) {
            index++;
            Output.out.print(FLAG_BEGIN);
        }

        if (args.length > index) {
            JCommander jCommander = commander.getCommands().get(args[index]);
            if (jCommander != null) {
                ((Command) jCommander.getObjects().get(0)).run();
            } else {
                commander.usage();
            }
        } else {
            commander.usage();
        }

        if (main.flag) {
            Output.out.print(FLAG_END);
        }
    }

    public static void main(String[] args) {
        if (Output.out != null) {
            Output.out.setPrintStream(System.out);
        }
        if (Output.err != null) {
            Output.err.setPrintStream(System.err);
        }

        try {
            parseArgs(args);
        } catch (Throwable th) {
            Output.err.print(th.getMessage());
            LogUtil.printErrStackTrace(TAG, th, null);
            System.exit(-1);
        }
    }

}
