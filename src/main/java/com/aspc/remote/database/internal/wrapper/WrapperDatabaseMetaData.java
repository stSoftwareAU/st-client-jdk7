/**
 *  STS Remote library
 *
 *  Copyright (C) 2006  stSoftware Pty Ltd
 *
 *  www.stsoftware.com.au
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *
 *  info AT stsoftware.com.au
 *
 *  or by snail mail to:
 *
 *  stSoftware
 *  building C, level 1,
 *  14 Rodborough Rd
 *  Frenchs Forest 2086
 *  Australia.
 */
package com.aspc.remote.database.internal.wrapper;

import java.sql.*;
import com.aspc.remote.util.misc.*;
import org.apache.commons.logging.Log;

/**
 *  soap statement
 *
 * <i>THREAD MODE: SINGLE-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       29 September 2006
 */
public class WrapperDatabaseMetaData implements DatabaseMetaData
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.database.internal.wrapper.WrapperDatabaseMetaData");//#LOGGER-NOPMD

    private final DatabaseMetaData databaseMetaData;

    /**
     *
     * @param databaseMetaData the meta data
     */
    public WrapperDatabaseMetaData( final DatabaseMetaData databaseMetaData)
    {
        this.databaseMetaData=databaseMetaData;
    }

    /** {@inheritDoc} */
    @Override
    public boolean allProceduresAreCallable() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean allTablesAreSelectable() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public String getURL() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public String getUserName() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean isReadOnly() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean nullsAreSortedHigh() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean nullsAreSortedLow() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean nullsAreSortedAtStart() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean nullsAreSortedAtEnd() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public String getDatabaseProductName() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public String getDatabaseProductVersion() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public String getDriverName() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public String getDriverVersion() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public int getDriverMajorVersion()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public int getDriverMinorVersion()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean usesLocalFiles() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean usesLocalFilePerTable() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsMixedCaseIdentifiers() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean storesUpperCaseIdentifiers() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean storesLowerCaseIdentifiers() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean storesMixedCaseIdentifiers() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public String getIdentifierQuoteString() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public String getSQLKeywords() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public String getNumericFunctions() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public String getStringFunctions() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public String getSystemFunctions() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public String getTimeDateFunctions() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public String getSearchStringEscape() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public String getExtraNameCharacters() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsAlterTableWithAddColumn() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsAlterTableWithDropColumn() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsColumnAliasing() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean nullPlusNonNullIsNull() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsConvert() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsConvert(int fromType, int toType) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsTableCorrelationNames() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsDifferentTableCorrelationNames() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsExpressionsInOrderBy() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsOrderByUnrelated() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsGroupBy() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsGroupByUnrelated() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsGroupByBeyondSelect() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsLikeEscapeClause() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsMultipleResultSets() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsMultipleTransactions() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsNonNullableColumns() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsMinimumSQLGrammar() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsCoreSQLGrammar() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsExtendedSQLGrammar() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsANSI92EntryLevelSQL() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsANSI92IntermediateSQL() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsANSI92FullSQL() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsIntegrityEnhancementFacility() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsOuterJoins() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsFullOuterJoins() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsLimitedOuterJoins() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public String getSchemaTerm() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public String getProcedureTerm() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public String getCatalogTerm() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCatalogAtStart() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public String getCatalogSeparator() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsSchemasInDataManipulation() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsSchemasInProcedureCalls() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsSchemasInTableDefinitions() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsSchemasInIndexDefinitions() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsCatalogsInDataManipulation() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsCatalogsInProcedureCalls() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsCatalogsInTableDefinitions() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsCatalogsInIndexDefinitions() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsPositionedDelete() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsPositionedUpdate() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsSelectForUpdate() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsStoredProcedures() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsSubqueriesInComparisons() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsSubqueriesInExists() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsSubqueriesInIns() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsSubqueriesInQuantifieds() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsCorrelatedSubqueries() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsUnion() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsUnionAll() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsOpenCursorsAcrossCommit() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsOpenCursorsAcrossRollback() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsOpenStatementsAcrossCommit() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsOpenStatementsAcrossRollback() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxBinaryLiteralLength() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxCharLiteralLength() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxColumnNameLength() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxColumnsInGroupBy() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxColumnsInIndex() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxColumnsInOrderBy() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxColumnsInSelect() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxColumnsInTable() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxConnections() throws SQLException
    {
        return databaseMetaData.getMaxConnections();
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxCursorNameLength() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxIndexLength() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxSchemaNameLength() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxProcedureNameLength() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxCatalogNameLength() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxRowSize() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxStatementLength() throws SQLException
    {
        return databaseMetaData.getMaxStatementLength();
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxStatements() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxTableNameLength() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxTablesInSelect() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxUserNameLength() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public int getDefaultTransactionIsolation() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsTransactions() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsTransactionIsolationLevel(int level) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsDataManipulationTransactionsOnly() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean dataDefinitionCausesTransactionCommit() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean dataDefinitionIgnoredInTransactions() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern) throws SQLException
    {
        return databaseMetaData.getProcedures(catalog, schemaPattern, procedureNamePattern);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws SQLException
    {
        return databaseMetaData.getTables(catalog, schemaPattern, tableNamePattern, types);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getSchemas() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getCatalogs() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getTableTypes() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getVersionColumns(String catalog, String schema, String table) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getPrimaryKeys(String catalog, String schema, String table) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getCrossReference(String parentCatalog, String parentSchema, String parentTable, String foreignCatalog, String foreignSchema, String foreignTable) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getTypeInfo() throws SQLException
    {
        return databaseMetaData.getTypeInfo();
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate) throws SQLException
    {
        if( approximate == false) throw new WrapperAssertError("approximate MUST be true otherwise Oracle will clear stats");

        return databaseMetaData.getIndexInfo(catalog, schema, table, unique, approximate);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsResultSetType(int type) throws SQLException
    {
        return databaseMetaData.supportsResultSetType(type);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean ownUpdatesAreVisible(int type) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean ownDeletesAreVisible(int type) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean ownInsertsAreVisible(int type) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean othersUpdatesAreVisible(int type) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean othersDeletesAreVisible(int type) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean othersInsertsAreVisible(int type) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean updatesAreDetected(int type) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean deletesAreDetected(int type) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean insertsAreDetected(int type) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsBatchUpdates() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public Connection getConnection() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsSavepoints() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsNamedParameters() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsMultipleOpenResults() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsGetGeneratedKeys() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern, String attributeNamePattern) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsResultSetHoldability(int holdability) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public int getResultSetHoldability() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public int getDatabaseMajorVersion() throws SQLException
    {
        return databaseMetaData.getDatabaseMajorVersion();
    }

    /** {@inheritDoc} */
    @Override
    public int getDatabaseMinorVersion() throws SQLException
    {
        return databaseMetaData.getDatabaseMinorVersion();
    }

    /** {@inheritDoc} */
    @Override
    public int getJDBCMajorVersion() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public int getJDBCMinorVersion() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public int getSQLStateType() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean locatorsUpdateCopy() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsStatementPooling() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public RowIdLifetime getRowIdLifetime() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean autoCommitFailureClosesAllResultSets() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getClientInfoProperties() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern) throws SQLException
    {
        return databaseMetaData.getFunctions(catalog, schemaPattern, functionNamePattern);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern, String columnNamePattern) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc } */
    @Override
    public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc } */
    @Override
    public boolean generatedKeyAlwaysReturned() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
