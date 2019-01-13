package org.ironman.framework.bean;

import android.content.pm.PackageParser;
import android.content.pm.ServiceInfo;
import android.text.TextUtils;

public class JService extends JComponent {

    public JPermission permission;

    public JService(PackageParser.Service service) {
        super(service, service.info);
        if (!TextUtils.isEmpty(service.info.permission)) {
            permission = new JPermission(service.info.permission);
        }
    }
}
