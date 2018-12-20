package android.tools.command;

import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.tools.Command;
import android.tools.Output;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hu on 18-12-18.
 */

@Parameters(commandDescription = "")
public class ServiceCommand extends Command {

    @Parameter(names = {"-l", "--list"}, order = 0, description = "List all system services")
    public boolean list = false;

    @Parameter(names = {"-s", "--simplify"}, order = 1, description = "Display Simplified information.")
    private boolean simplify = false;

    @Parameter(names = {"-f", "--fuzz"}, order = 100, variableArity = true, description = "Fuzz system services")
    public List<String> fuzz = new ArrayList<>();

    @Parameter(names = {"-e", "--except-mode"}, order = 101, description = "Fuzz system services (except mode)")
    public boolean except = false;

    @Override
    public void run() {
        String[] services = null;

        try {
            services = ServiceManager.listServices();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        if (services == null || services.length == 0) {
            return;
        }

        for (String name : services) {

            boolean needFuzz = inFuzzList(name);

            if (!needFuzz && !list) {
                continue;
            }

            Service service = new Service(name);
            service.print(simplify);

            if (!service.valid() || !needFuzz) {
                continue;
            }

            service.fuzz();

            Output.out.println();
        }
    }

    private boolean inFuzzList(String service) {
        boolean contains = fuzz.contains(service);
        return !(!except && !contains) && !(except && contains);
    }

    private static class Service {

        String name = "";
        String desc = "";
        IBinder binder = null;

        Service(String name) {
            this.name = name;
            try {
                this.binder = ServiceManager.getService(name);
                this.desc = this.binder.getInterfaceDescriptor();
            } catch (Exception e) {
                this.binder = null;
                this.desc = "";
            }
        }

        boolean valid() {
            return binder != null && !TextUtils.isEmpty(desc);
        }

        void print(boolean simplify) {
            if (!simplify) {
                Output.out.println("[*] %s: [%s]", name, desc);
            } else {
                Output.out.println(name);
            }
        }

        void fuzz() {
            Parcel data = Parcel.obtain();
            data.writeInterfaceToken(desc);
//            while (data.dataSize() < 0x1000) {
//                data.writeInt(0);
//            }

            for (int i = 1; i <= 1000; i++) {
                Parcel reply = Parcel.obtain();
                try {
                    if (binder.transact(i, data, reply, 0)) {
                        try {
                            reply.readException();
                            Output.out.indent(4).println("%d", i);
                        } catch (Exception e) {
                            Output.out.indent(4).println("%d -> %s: %s",
                                    i, e.getClass().getName(), e.getMessage());
                        }
                    }
                } catch (RemoteException e) {
                    Output.out.indent(4).println("%d -> %s: %s",
                            i, e.getClass().getName(), e.getMessage());
                    if (e instanceof DeadObjectException) {
                        Output.out.indent(4).println("%s service is died", name);
                        break;
                    }
                } catch (Exception e) {
                    // e.printStackTrace();
                }
                reply.recycle();
            }
            data.recycle();
        }
    }
}
