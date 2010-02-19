/*
 * This file is part of DRBD Management Console by LINBIT HA-Solutions GmbH
 * written by Rasto Levrinc.
 *
 * Copyright (C) 2009-2010, LINBIT HA-Solutions GmbH.
 * Copyright (C) 2009-2010, Rasto Levrinc
 *
 * DRBD Management Console is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * DRBD Management Console is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with drbd; see the file COPYING.  If not, write to
 * the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package drbd.gui.resources;

import drbd.gui.Browser;
import drbd.gui.HostBrowser;
import drbd.gui.ClusterBrowser;
import drbd.data.VMSXML;
import drbd.data.Host;
import drbd.utilities.Tools;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.Comparator;
import java.awt.Color;

/**
 * This class shows a list of virtual machines.
 */
public class VMSInfo extends CategoryInfo {
    /** On what raw is the vms virtual domain info object. */
    private volatile Map<String, VMSVirtualDomainInfo> domainToInfo =
                                   new HashMap<String, VMSVirtualDomainInfo>();
    /** Colors for some rows. */
    private volatile Map<String, Color> domainToColor =
                                                  new HashMap<String, Color>();
    /**
     * Creates the new VMSInfo object with name of the category.
     */
    public VMSInfo(final String name, final Browser browser) {
        super(name, browser);
    }

    /**
     * Returns browser object of this info.
     */
    protected final ClusterBrowser getBrowser() {
        return (ClusterBrowser) super.getBrowser();
    }

    /**
     * Returns columns for the table.
     */
    protected final String[] getColumnNames() {
        return new String[]{"Name", "Defined on", "Status", "Memory"};
    }

    /**
     * Returns data for the table.
     */
    protected final Object[][] getTableData() {
        final List<Object[]> rows = new ArrayList<Object[]>();
        final Set<String> domainNames = new TreeSet<String>();
        for (final Host host : getBrowser().getClusterHosts()) {
            final VMSXML vxml = getBrowser().getVMSXML(host);
            if (vxml != null) {
                domainNames.addAll(vxml.getDomainNames());
            }
        }
        final Map<String, VMSVirtualDomainInfo> dti =
                                   new HashMap<String, VMSVirtualDomainInfo>();
        final Map<String, Color> dtc = new HashMap<String, Color>();
        for (final String domainName : domainNames) {
            for (final Host host : getBrowser().getClusterHosts()) {
                final VMSXML vxml = getBrowser().getVMSXML(host);
                if (vxml != null && vxml.isRunning(domainName)) {
                    final Color bgColor = host.getPmColors()[0];
                    dtc.put(domainName, bgColor);
                    break;
                }
            }
            final VMSVirtualDomainInfo vmsvdi =
                        getBrowser().findVMSVirtualDomainInfo(domainName);
            dti.put(domainName, vmsvdi);
            final JLabel domainNameLabel =
                               new JLabel(HostBrowser.HOST_ICON_LARGE);
            domainNameLabel.setOpaque(true);
            domainNameLabel.setText(domainName);
            rows.add(new Object[]{domainNameLabel,
                                  vmsvdi.getDefinedOnString(),
                                  vmsvdi.getRunningOnString(),
                                  vmsvdi.getResource().getValue("memory")});
        }
        domainToInfo = dti;
        domainToColor = dtc;
        return rows.toArray(new Object[rows.size()][]);
    }

    /**
     * Execute when row in the table was clicked.
     */
    protected final void rowClicked(final String key) {
        final VMSVirtualDomainInfo vmsvdi = domainToInfo.get(key);
        if (vmsvdi != null) {
            vmsvdi.selectMyself();
        }
    }

    /**
     * Alignment for the specified column.
     */
    protected final int getColumnAlignment(final int column) {
        if (column == 3) {
            return SwingConstants.RIGHT;
        }
        return SwingConstants.LEFT;
    }

    /**
     * Selects the node in the menu.
     */
    public final void selectMyself() {
        super.selectMyself();
        getBrowser().nodeChanged(getNode());
    }

    /**
     * Returns row height for the table.
     */
    protected final int getRowHeight() {
        return 32;
    }

    /**
     * Retrurns color for some rows.
     */
    protected final Color getColorForRow(final String key) {
        return domainToColor.get(key);
    }

    /**
     * Returns comparator for column.
     */
    protected final Comparator<String> getColComparator(final int col) {
        if (col == 3) {
            /* memory */
            final Comparator<String> c = new Comparator<String>() {
                public int compare(final String s1, final String s2) {
                    final int i1 = Tools.convertToKilobytes(s1);
                    final int i2 = Tools.convertToKilobytes(s2);
                    if (i1 < i2) {
                        return -1;
                    } else if (i1 > i2) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            };
            return c;
        }
        return null;
    }
}
