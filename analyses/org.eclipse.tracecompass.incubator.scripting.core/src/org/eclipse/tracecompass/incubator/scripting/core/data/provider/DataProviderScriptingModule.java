/*******************************************************************************
 * Copyright (c) 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.scripting.core.data.provider;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.ease.modules.WrapToScript;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.incubator.internal.scripting.core.data.provider.ScriptedEntryDataModel;
import org.eclipse.tracecompass.incubator.internal.scripting.core.data.provider.ScriptedTimeGraphDataProvider;
import org.eclipse.tracecompass.incubator.internal.scripting.core.data.provider.ScriptingDataProviderManager;
import org.eclipse.tracecompass.incubator.scripting.core.analysis.ScriptedAnalysis;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenStateSystemPath;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValueConstant;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output.DataDrivenOutputEntry;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output.DataDrivenTimeGraphProviderFactory;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output.DataDrivenXYDataProvider.DisplayType;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphArrow;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphDataProvider;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphRowModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphArrow;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;

/**
 * Scripting module to create data providers from scripted analyzes. Data
 * providers are used to define what and how data will be exported, either as
 * views or other means of exportation.
 * <p>
 * Example scripts with data providers can be found here:
 * <ul>
 * <li><a href="../../doc-files/scriptedDataProvider.js">A scripted
 * time graph data provider</a> with script-defined entries and arrows</li>
 * <li><a href="../../doc-files/basicAnalysis.js">A basic analysis</a>
 * building an state system and showing its data in a time graph</li>
 * </ul>
 *
 * @author Geneviève Bastien
 */
@SuppressWarnings("restriction")
public class DataProviderScriptingModule {

    /** Path key to create data providers */
    @WrapToScript
    public static final String ENTRY_PATH = "path"; //$NON-NLS-1$
    /** Display key to create data providers */
    @WrapToScript
    public static final String ENTRY_DISPLAY = "display"; //$NON-NLS-1$
    /** Name key to create data providers */
    @WrapToScript
    public static final String ENTRY_NAME = "name"; //$NON-NLS-1$
    /** Parent key to create data providers */
    @WrapToScript
    public static final String ENTRY_PARENT = "parent"; //$NON-NLS-1$
    /** Id key to create data providers */
    @WrapToScript
    public static final String ENTRY_ID = "id"; //$NON-NLS-1$

    /** Quark key to create entries */
    @WrapToScript
    public static final String ENTRY_FIELD_QUARK = "quark"; //$NON-NLS-1$
    /** Parent ID key to create entries */
    @WrapToScript
    public static final String ENTRY_FIELD_PARENT_ID = "parentId"; //$NON-NLS-1$

    /**
     * Create a data driven time graph provider from an analysis's state system.
     * This will use the specified data to get the entries and row data from the
     * state system. When the data cannot be obtained in a straight-forward
     * manner from the state system, the
     * {@link #createScriptedTimeGraphProvider(ScriptedAnalysis, Function, Function, Function)}
     * method can be used instead.
     * <p>
     * The possible keys for the data are:
     * </p>
     * <ul>
     * <li>{@link #ENTRY_PATH}: MANDATORY, specifies the path in the state
     * system (including wildcards) that is the root of the entries. For all
     * root attributes, use '*'</li>
     * <li>{@link #ENTRY_DISPLAY}: The path from the entry's root of the
     * attribute to display. If not specified, the root attribute itself will be
     * used</li>
     * <li>{@link #ENTRY_NAME}: The path from the entry's root of the attribute
     * that contains the name. If not specified, the name will be the
     * attribute's name.</li>
     * <li>{@link #ENTRY_ID}: The path from the entry's root of the attribute
     * that contains an identifier for this entry. The identifier can be used to
     * build hierarchies of entries using the {@link #ENTRY_PARENT}.</li>
     * <li>{@link #ENTRY_PARENT}: The path from the entry's root of the
     * attribute that contains the parent's ID. This data will be used along
     * with the {@link #ENTRY_ID} to create a hierarchy between the entries.
     * </li>
     * </ul>
     *
     * @param analysis
     *            The analysis for which to create a time graph provider
     * @param data
     *            The time graph provider data
     * @return The time graph data provider
     */
    @WrapToScript
    public @Nullable ITimeGraphDataProvider<TimeGraphEntryModel> createTimeGraphProvider(ScriptedAnalysis analysis, Map<String, Object> data) {
        Object pathObj = data.get(ENTRY_PATH);
        if (pathObj == null) {
            return null;
        }
        String path = String.valueOf(pathObj);
        Object displayObj = data.get(ENTRY_DISPLAY);
        DataDrivenStateSystemPath display = new DataDrivenStateSystemPath(displayObj == null ? Collections.emptyList() : Collections.singletonList(new DataDrivenValueConstant(null, ITmfStateValue.Type.NULL, String.valueOf(displayObj))));

        Object nameObj = data.get(ENTRY_NAME);
        DataDrivenStateSystemPath name = (nameObj == null) ? null : new DataDrivenStateSystemPath(Collections.singletonList(new DataDrivenValueConstant(null, ITmfStateValue.Type.NULL, String.valueOf(nameObj))));

        Object parentObj = data.get(ENTRY_PARENT);
        DataDrivenStateSystemPath parent = (parentObj == null) ? null : new DataDrivenStateSystemPath(Collections.singletonList(new DataDrivenValueConstant(null, ITmfStateValue.Type.NULL, String.valueOf(parentObj))));

        Object idObj = data.get(ENTRY_ID);
        DataDrivenStateSystemPath id = new DataDrivenStateSystemPath(Collections.singletonList(new DataDrivenValueConstant(null, ITmfStateValue.Type.NULL, String.valueOf(idObj))));

        DataDrivenOutputEntry entry = new DataDrivenOutputEntry(Collections.emptyList(), path, null, true,
                display, id, parent, name, DisplayType.ABSOLUTE);
        DataDrivenTimeGraphProviderFactory factory = new DataDrivenTimeGraphProviderFactory(Collections.singletonList(entry), Collections.singleton(analysis.getName()), Collections.emptyList());
        ITmfStateSystemBuilder stateSystem = analysis.getStateSystem(true);
        if (stateSystem == null) {
            return null;
        }
        ITimeGraphDataProvider<TimeGraphEntryModel> provider = factory.create(analysis.getTrace(), Collections.singletonList(stateSystem), ScriptingDataProviderManager.PROVIDER_ID + ':' + analysis.getName());
        ScriptingDataProviderManager.getInstance().registerDataProvider(analysis.getTrace(), provider);
        return provider;
    }

    /**
     * Create a time graph entry. This method will typically be used by scripts
     * to create entries for data provider generated by the
     * {@link #createScriptedTimeGraphProvider(ScriptedAnalysis, Function, Function, Function)}
     * method.
     *
     * @param name
     *            The name (label) of the entry
     * @param data
     *            A map of fields to create the entry. Possible fields are
     *            {@link #ENTRY_FIELD_QUARK} (quark in the state system to use
     *            for the data. If not specified, it is expected the data
     *            provider will provide a method to retrieve the data), and
     *            {@link #ENTRY_FIELD_PARENT_ID} for the ID of the parent entry
     * @return A new entry model
     */
    @WrapToScript
    public @Nullable ITmfTreeDataModel createEntry(String name, Map<String, Object> data) {
        Object quarkObj = data.get(ENTRY_FIELD_QUARK);
        int quark = (!(quarkObj instanceof Number)) ? ITmfStateSystem.INVALID_ATTRIBUTE : ((Number) quarkObj).intValue();
        Object parentObj = data.get(ENTRY_FIELD_PARENT_ID);
        int parent = (!(parentObj instanceof Number)) ? -1 : ((Number) parentObj).intValue();

        return new ScriptedEntryDataModel(name, parent, quark);
    }

    /**
     * Create an arrow for a time graph data provider. This method will
     * typically be used by scripts to create entries for data provider
     * generated by the
     * {@link #createScriptedTimeGraphProvider(ScriptedAnalysis, Function, Function, Function)}
     * method.
     *
     * @param sourceId
     *            the ID of the source entry of the arrow
     * @param destinationId
     *            The ID of the destination entry of the arrow
     * @param time
     *            The start time of the arrow
     * @param duration
     *            The duration of the arrow
     * @param value
     *            The value to associate with this arrow
     * @return The new arrow
     */
    @WrapToScript
    public @Nullable ITimeGraphArrow createArrow(long sourceId, long destinationId, long time, long duration, int value) {
        return new TimeGraphArrow(sourceId, destinationId, time, duration, value);
    }

    /**
     * Create a data provider from scripted functions. The script itself is
     * responsible for generating the entries, optionally row data and arrows.
     * For a simple state system, the
     * {@link #createTimeGraphProvider(ScriptedAnalysis, Map)} may be used
     * instead
     *
     * @param analysis
     *            The analysis this data provider is for
     * @param entryMethod
     *            The function this data provider will use to get the entries.
     *            This parameter is mandatory.
     * @param rowModelMethod
     *            The function this data provider will use to get the row data
     *            for time ranges. If none is specified, the entries are
     *            expected to have a quark indicating which row in the state
     *            system to use for the data.
     * @param arrowMethod
     *            The function this data provider will use to get the arrow data
     *            for time ranges. If none is specified, no arrows will be
     *            drawn.
     * @return A time graph data provider
     */
    @WrapToScript
    public ITimeGraphDataProvider<ITimeGraphEntryModel> createScriptedTimeGraphProvider(ScriptedAnalysis analysis,
            Function<Map<String, Object>, @Nullable List<ITimeGraphEntryModel>> entryMethod,
            @Nullable Function<Map<String, Object>, @Nullable List<ITimeGraphRowModel>> rowModelMethod,
            @Nullable Function<Map<String, Object>, @Nullable List<ITimeGraphArrow>> arrowMethod) {
        ITimeGraphDataProvider<ITimeGraphEntryModel> provider = new ScriptedTimeGraphDataProvider(analysis, entryMethod, rowModelMethod, arrowMethod);
        ScriptingDataProviderManager.getInstance().registerDataProvider(analysis.getTrace(), provider);
        return provider;
    }

}