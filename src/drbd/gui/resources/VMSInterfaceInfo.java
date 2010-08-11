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
import drbd.gui.GuiComboBox;
import drbd.data.VMSXML;
import drbd.data.VMSXML.InterfaceData;
import drbd.data.Host;
import drbd.data.ConfigData;
import drbd.utilities.Tools;

import javax.swing.JPanel;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;

/**
 * This class holds info about Virtual Interfaces.
 */
public class VMSInterfaceInfo extends VMSHardwareInfo {
    /** Source network combo box, so that it can be disabled, depending on
     * type. */
    private GuiComboBox sourceNetworkCB = null;
    /** Source bridge combo box, so that it can be disabled, depending on
     * type. */
    private GuiComboBox sourceBridgeCB = null;
    /** Previous value of the type (network or bridge). */
    private String prevType = null;
    /** Parameters. */
    private static final String[] PARAMETERS = {InterfaceData.TYPE,
                                                InterfaceData.MAC_ADDRESS,
                                                InterfaceData.SOURCE_NETWORK,
                                                InterfaceData.SOURCE_BRIDGE,
                                                InterfaceData.TARGET_DEV,
                                                InterfaceData.MODEL_TYPE};
    /** Field type. */
    private static final Map<String, GuiComboBox.Type> FIELD_TYPES =
                                       new HashMap<String, GuiComboBox.Type>();
    /** Short name. */
    private static final Map<String, String> SHORTNAME_MAP =
                                                 new HashMap<String, String>();
    static {
        FIELD_TYPES.put(InterfaceData.TYPE,
                        GuiComboBox.Type.RADIOGROUP);
        SHORTNAME_MAP.put(InterfaceData.TYPE, "Type");
        SHORTNAME_MAP.put(InterfaceData.MAC_ADDRESS, "Mac Address");
        SHORTNAME_MAP.put(InterfaceData.SOURCE_NETWORK, "Source Network");
        SHORTNAME_MAP.put(InterfaceData.SOURCE_BRIDGE, "Source Bridge");
        SHORTNAME_MAP.put(InterfaceData.TARGET_DEV, "Target Device");
        SHORTNAME_MAP.put(InterfaceData.MODEL_TYPE, "Model Type");
    }

    /** Whether the parameter is editable only in advanced mode. */
    private static final Set<String> IS_ENABLED_ONLY_IN_ADVANCED =
        new HashSet<String>(Arrays.asList(new String[]{
                                                InterfaceData.MAC_ADDRESS,
                                                InterfaceData.TARGET_DEV,
                                                InterfaceData.MODEL_TYPE}));

    /** Whether the parameter is required. */
    private static final Set<String> IS_REQUIRED =
        new HashSet<String>(Arrays.asList(new String[]{
                                                InterfaceData.TYPE }));

    /** Default name. */
    private static final Map<String, String> DEFAULTS_MAP =
                                                 new HashMap<String, String>();
    /** Possible values. */
    private static final Map<String, Object[]> POSSIBLE_VALUES =
                                               new HashMap<String, Object[]>();
    static {
        DEFAULTS_MAP.put(InterfaceData.MAC_ADDRESS, "generate");
        POSSIBLE_VALUES.put(InterfaceData.MODEL_TYPE,
                            new String[]{null,
                                         "default",
                                         "e1000",
                                         "ne2k_pci",
                                         "pcnet",
                                         "rtl8139",
                                         "virtio"});
    }
    /** Creates the VMSInterfaceInfo object. */
    public VMSInterfaceInfo(final String name, final Browser browser,
                            final VMSVirtualDomainInfo vmsVirtualDomainInfo) {
        super(name, browser, vmsVirtualDomainInfo);
    }

    /** Adds disk table with only this disk to the main panel. */
    protected void addHardwareTable(final JPanel mainPanel) {
        mainPanel.add(getTablePanel("Interfaces",
                                    VMSVirtualDomainInfo.INTERFACES_TABLE));
    }

    /** Returns service icon in the menu. */
    public final ImageIcon getMenuIcon(final boolean testOnly) {
        return NetInfo.NET_I_ICON;
    }

    /** Returns long description of the specified parameter. */
    protected final String getParamLongDesc(final String param) {
        return SHORTNAME_MAP.get(param);
    }

    /** Returns short description of the specified parameter. */
    protected final String getParamShortDesc(final String param) {
        return SHORTNAME_MAP.get(param);
    }

    /** Returns preferred value for specified parameter. */
    protected final String getParamPreferred(final String param) {
        return null;
    }

    /** Returns default value for specified parameter. */
    protected final String getParamDefault(final String param) {
        return DEFAULTS_MAP.get(param);
    }

    /** Returns parameters. */
    public final String[] getParametersFromXML() {
        return PARAMETERS;
    }

    /** Returns possible choices for drop down lists. */
    protected final Object[] getParamPossibleChoices(final String param) {
        if (InterfaceData.SOURCE_NETWORK.equals(param)) {
            for (final Host h : getBrowser().getClusterHosts()) {
                final VMSXML vmsxml = getBrowser().getVMSXML(h);
                if (vmsxml != null) {
                    final List<String> networks = vmsxml.getNetworks();
                    networks.add(0, null);
                    return networks.toArray(new String[networks.size()]);
                }
            }
        } else if (InterfaceData.SOURCE_BRIDGE.equals(param)) {
            for (final Host h : getBrowser().getClusterHosts()) {
                final VMSXML vmsxml = getBrowser().getVMSXML(h);
                if (vmsxml != null) {
                    final List<String> bridges = h.getBridges();
                    bridges.add(0, null);
                    return bridges.toArray(new String[bridges.size()]);
                }
            }
        } else if (InterfaceData.TYPE.equals(param)) {
            return new String[]{"network", "bridge"};
        }
        return POSSIBLE_VALUES.get(param);
    }

    /** Returns section to which the specified parameter belongs. */
    protected final String getSection(final String param) {
        return "Interface Options";
    }

    /** Returns true if the specified parameter is required. */
    protected final boolean isRequired(final String param) {
        final String type = getComboBoxValue(InterfaceData.TYPE);
        if ((InterfaceData.SOURCE_NETWORK.equals(param)
             && "network".equals(type))
            || (InterfaceData.SOURCE_BRIDGE.equals(param)
                && "bridge".equals(type))) {
            return true;
        }
        return IS_REQUIRED.contains(param);
    }

    /** Returns true if the specified parameter is integer. */
    protected final boolean isInteger(final String param) {
        return false;
    }

    /** Returns true if the specified parameter is label. */
    protected final boolean isLabel(final String param) {
        return false;
    }

    /** Returns true if the specified parameter is of time type. */
    protected final boolean isTimeType(final String param) {
        return false;
    }

    /** Returns whether parameter is checkbox. */
    protected final boolean isCheckBox(final String param) {
        return false;
    }

    /** Returns the type of the parameter. */
    protected final String getParamType(final String param) {
        return "undef"; // TODO:
    }

    /** Returns the regexp of the parameter. */
    protected final String getParamRegexp(final String param) {
        if (VMSXML.InterfaceData.MAC_ADDRESS.equals(param)) {
            return "^((([0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2})|generate)?$";
        }
        return null;
    }

    /** Returns type of the field. */
    protected final GuiComboBox.Type getFieldType(final String param) {
        return FIELD_TYPES.get(param);
    }

    /** Applies the changes. */
    public final void apply(final boolean testOnly) {
        if (testOnly) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                applyButton.setEnabled(false);
            }
        });
        final String[] params = getParametersFromXML();
        final Map<String, String> parameters = new HashMap<String, String>();
        String type = null;
        for (final String param : getParametersFromXML()) {
            final String value = getComboBoxValue(param);
            if (InterfaceData.TYPE.equals(param)) {
                type = value;
            }
            if ("network".equals(type)
                && InterfaceData.SOURCE_BRIDGE.equals(param)) {
                getResource().setValue(param, null);
                continue;
            } else if ("bridge".equals(type)
                && InterfaceData.SOURCE_NETWORK.equals(param)) {
                getResource().setValue(param, null);
                continue;
            }
            if (!Tools.areEqual(getParamSaved(param), value)) {
                parameters.put(param, value);
                getResource().setValue(param, value);
            }
        }
        for (final Host h : getBrowser().getClusterHosts()) {
            final VMSXML vmsxml = getBrowser().getVMSXML(h);
            if (vmsxml != null) {
                vmsxml.modifyInterfaceXML(
                                    getVMSVirtualDomainInfo().getDomainName(),
                                    getName(),
                                    parameters);
            }
            getResource().setNew(false);
            setName(getParamSaved(InterfaceData.MAC_ADDRESS));
        }
        for (final Host h : getBrowser().getClusterHosts()) {
            getBrowser().periodicalVMSUpdate(h);
        }
        checkResourceFields(null, params);
        getBrowser().reload(getNode());
    }

    /** Returns data for the table. */
    protected final Object[][] getTableData(final String tableName) {
        if (VMSVirtualDomainInfo.HEADER_TABLE.equals(tableName)) {
            return getVMSVirtualDomainInfo().getMainTableData();
        } else if (VMSVirtualDomainInfo.INTERFACES_TABLE.equals(tableName)) {
            if (getResource().isNew()) {
                return new Object[][]{};
            }
            return new Object[][]{getVMSVirtualDomainInfo().getInterfaceDataRow(
                                    getName(),
                                    null,
                                    getVMSVirtualDomainInfo().getInterfaces(),
                                    true)};
        }
        return new Object[][]{};
    }

    /** Returns whether this parameter is advanced. */
    protected final boolean isAdvanced(final String param) {
        return false;
    }

    /** Whether the parameter should be enabled. */
    protected final boolean isEnabled(final String param) {
         return !IS_ENABLED_ONLY_IN_ADVANCED.contains(param)
                || Tools.getConfigData().getExpertMode();
    }

    /** Returns access type of this parameter. */
    protected final ConfigData.AccessType getAccessType(final String param) {
        return ConfigData.AccessType.ADMIN;
    }

    /** Returns true if the value of the parameter is ok. */
    protected final boolean checkParam(final String param,
                                       final String newValue) {
        if (InterfaceData.TYPE.equals(param)) {
            if (!newValue.equals(prevType)) {
                if (sourceNetworkCB != null) {
                    sourceNetworkCB.setVisible("network".equals(newValue));
                }
                if (sourceBridgeCB != null) {
                    sourceBridgeCB.setVisible("bridge".equals(newValue));
                }
                prevType = newValue;
                checkResourceFields(InterfaceData.SOURCE_NETWORK,
                                    getParametersFromXML());
                checkResourceFields(InterfaceData.SOURCE_BRIDGE,
                                    getParametersFromXML());
            }
        }
        if (isRequired(param) && (newValue == null || "".equals(newValue))) {
            return false;
        }
        return true;
    }
    /** Returns combo box for parameter. */
    protected final GuiComboBox getParamComboBox(final String param,
                                           final String prefix,
                                           final int width) {
        final GuiComboBox paramCB = super.getParamComboBox(param,
                                                           prefix,
                                                           width);
        if (InterfaceData.TYPE.equals(param)) {
            paramCB.setAlwaysEditable(false);
        } else if (InterfaceData.SOURCE_NETWORK.equals(param)) {
            sourceNetworkCB = paramCB;
            paramCB.setAlwaysEditable(false);
        } else if (InterfaceData.SOURCE_BRIDGE.equals(param)) {
            sourceBridgeCB = paramCB;
            paramCB.setAlwaysEditable(false);
        }
        return paramCB;
    }

    /** Updates parameters. */
    public final void updateParameters() {
        final Map<String, InterfaceData> interfaces =
                                    getVMSVirtualDomainInfo().getInterfaces();
        if (interfaces != null) {
            final InterfaceData interfaceData = interfaces.get(getName());
            if (interfaceData != null) {
                for (final String param : getParametersFromXML()) {
                    final String oldValue = getParamSaved(param);
                    String value = getParamSaved(param);
                    final GuiComboBox cb = paramComboBoxGet(param, null);
                    for (final Host h : getBrowser().getClusterHosts()) {
                        final VMSXML vmsxml = getBrowser().getVMSXML(h);
                        if (vmsxml != null) {
                            final String savedValue =
                                               interfaceData.getValue(param);
                            if (savedValue != null) {
                                value = savedValue;
                            }
                        }
                    }
                    if (!Tools.areEqual(value, oldValue)) {
                        getResource().setValue(param, value);
                        if (cb != null) {
                            /* only if it is not changed by user. */
                            cb.setValue(value);
                        }
                    }
                }
            }
        }
        updateTable(VMSVirtualDomainInfo.HEADER_TABLE);
        updateTable(VMSVirtualDomainInfo.INTERFACES_TABLE);
    }

    /** Returns string representation. */
    public String toString() {
        final StringBuffer s = new StringBuffer(30);
        String source;
        if ("network".equals(getParamSaved(InterfaceData.TYPE))) {
            source = getParamSaved(InterfaceData.SOURCE_NETWORK);
        } else {
            source = getParamSaved(InterfaceData.SOURCE_BRIDGE);
        }
        if (source == null) {
            s.append("new interface...");
        } else {
            s.append(source);
        }

        final String saved = getParamSaved(InterfaceData.MAC_ADDRESS);
        if (saved != null) {
            s.append(" (");
            if (saved.length() > 8) {
                s.append(saved.substring(8));
            } else {
                s.append(saved);
            }
            s.append(')');
        }
        return s.toString();
    }

    /** Removes this interface without confirmation dialog. */
    protected void removeMyselfNoConfirm(final boolean testOnly) {
        if (testOnly) {
            return;
        }
        for (final Host h : getBrowser().getClusterHosts()) {
            final VMSXML vmsxml = getBrowser().getVMSXML(h);
            if (vmsxml != null) {
                vmsxml.removeInterfaceXML(
                                    getVMSVirtualDomainInfo().getDomainName(),
                                    getName());
            }
        }
        for (final Host h : getBrowser().getClusterHosts()) {
            getBrowser().periodicalVMSUpdate(h);
        }
    }
}