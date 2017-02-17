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
package com.aspc.remote.jdbc.impl;

import com.aspc.remote.database.internal.SResultSet;
import com.aspc.remote.jdbc.SoapResultSet;
import com.aspc.remote.jdbc.SoapSQLException;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.StringUtilities;
import java.sql.*;
import java.util.Arrays;
import org.apache.commons.logging.Log;

/**
 * Remote Server database connection.
 * Implements a database connection through SOAP.
 *
 *  <i>THREAD MODE: SINGLE-THREADED</i>
 *
 * @author  Nigel Leck
 * @since 29 September 2006
 */
public class ExecutorDatabaseMetaData implements DatabaseMetaData
{
    private final ExecutorConnection connection;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.jdbc.impl.ExecutorDatabaseMetaData");//#LOGGER-NOPMD

    /**
     * the meta data
     * @param connection the connection
     */
    public ExecutorDatabaseMetaData( final ExecutorConnection connection)
    {
        this.connection = connection;
    }

    /** {@inheritDoc} */
    @Override
    public boolean allProceduresAreCallable() throws SQLException
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean allTablesAreSelectable() throws SQLException
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean dataDefinitionCausesTransactionCommit() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean dataDefinitionIgnoredInTransactions() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean deletesAreDetected(int type) throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern, String attributeNamePattern) throws SQLException
    {
        throw new SoapSQLException( "getAttributes(String catalog, String schemaPattern, String typeNamePattern, String attributeNamePattern) - Not Supported");
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable) throws SQLException
    {
        try
        (Statement statement = connection.createStatement()) {

            SoapResultSet r = (SoapResultSet) statement.executeQuery(
                "BEST_ROW_IDENTIFIER CATALOG " +
                sqlStr(catalog) + " SCHEMA " + sqlStr(schema) + " TABLE " + sqlStr(table) + " SCOPE "  +
                scope + " NULLABLE " + nullable
            );

            r.load();

            return r;
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getCatalogSeparator() throws SQLException
    {
        return "";
    }

    /** {@inheritDoc} */
    @Override
    public String getCatalogTerm() throws SQLException
    {
        return "catalog";
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getCatalogs() throws SQLException
    {
        try
        (Statement statement = connection.createStatement()) {

            SoapResultSet r = (SoapResultSet) statement.executeQuery(
                "CATALOGS"
            );

            r.load();

            return r;
        }
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getColumnPrivileges(final String catalog, final String schema, final String table, final String columnNamePattern) throws SQLException
    {
        try
        (Statement statement = connection.createStatement()) {

            SoapResultSet r = (SoapResultSet) statement.executeQuery("COLUMN_PRIVILEGES CATALOG " +
                    sqlStr(catalog) + " SCHEMA " + sqlStr(schema) + " TABLE " + sqlStr(table) + " PATTERN "  +
                    sqlStr(columnNamePattern));

            r.load();

            return r;
        }
    }

    private String sqlStr( final String value)
    {
        String temp = value;

        if( value == null) temp ="";

        return "'" + StringUtilities.replace(temp, "'", "\\'") + "'";
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException
    {
        try
        (Statement statement = connection.createStatement()) {

            SoapResultSet r = (SoapResultSet) statement.executeQuery("COLUMNS CATALOG " +
                    sqlStr(catalog) + " SCHEMA " + sqlStr(schemaPattern) + " TABLE " + sqlStr(tableNamePattern) + " COLUMN "  +
                    sqlStr(columnNamePattern));

            r.load();

            return r;
        }
    }

    /** {@inheritDoc} */
    @Override
    public Connection getConnection() throws SQLException
    {
        throw new SoapSQLException( "getConnection() - Not Supported");
    }

    /** {@inheritDoc}
     * @param primaryCatalog
     * @param primarySchema
     * @param primaryTable
     */
    @Override
    public ResultSet getCrossReference(
        String primaryCatalog,
        String primarySchema,
        String primaryTable,
        String foreignCatalog,
        String foreignSchema,
        String foreignTable
    ) throws SQLException
    {
        try
        (Statement statement = connection.createStatement()) {

            SoapResultSet r = (SoapResultSet) statement.executeQuery(
                "CROSS_REFERENCE" +
                " PRIMARY_CATALOG " + sqlStr(primaryCatalog) +
                " PRIMARY_SCHEMA " + sqlStr(primarySchema) +
                " PRIMARY_TABLE " + sqlStr(primaryTable) +
                " FOREIGN_CATALOG " + sqlStr(foreignCatalog) +
                " FOREIGN_SCHEMA " + sqlStr(foreignSchema) +
                " FOREIGN_TABLE " + sqlStr(foreignTable)
            );

            r.load();

            return r;
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getDatabaseMajorVersion() throws SQLException
    {
        return 1;
    }

    /** {@inheritDoc} */
    @Override
    public int getDatabaseMinorVersion() throws SQLException
    {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public String getDatabaseProductName() throws SQLException
    {
        return "ASPC";
    }

    /** {@inheritDoc} */
    @Override
    public String getDatabaseProductVersion() throws SQLException
    {
        return "0.1";
    }

    /** {@inheritDoc} */
    @Override
    public int getDefaultTransactionIsolation() throws SQLException
    {
        return Connection.TRANSACTION_READ_COMMITTED;
    }

    /** {@inheritDoc} */
    @Override
    public int getDriverMajorVersion()
    {
        return 1;
    }

    /** {@inheritDoc} */
    @Override
    public int getDriverMinorVersion()
    {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public String getDriverName() throws SQLException
    {
        return "ASPC JDBC";
    }

    /** {@inheritDoc} */
    @Override
    public String getDriverVersion() throws SQLException
    {
        return "0.1";
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getExportedKeys(final String catalog, final String schema, final String table) throws SQLException
    {
        try
        (Statement statement = connection.createStatement()) {

            SoapResultSet r = (SoapResultSet) statement.executeQuery(
                "EXPORTED_KEYS CATALOG " +
                sqlStr(catalog) + " SCHEMA " + sqlStr(schema) + " TABLE " + sqlStr(table)
            );

            r.load();

            return r;
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getExtraNameCharacters() throws SQLException
    {
        return "";
    }

    /** {@inheritDoc} */
    @Override
    public String getIdentifierQuoteString() throws SQLException
    {
        return " ";
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getImportedKeys(final String catalog, final String schema, final String table) throws SQLException
    {
        try
        (Statement statement = connection.createStatement()) {

            SoapResultSet r = (SoapResultSet) statement.executeQuery(
                "IMPORTED_KEYS CATALOG " +
                sqlStr(catalog) + " SCHEMA " + sqlStr(schema) + " TABLE " + sqlStr(table)
            );

            r.load();

            return r;
        }
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getIndexInfo(
        final String catalog,
        final String schema,
        final String table,
        final boolean unique,
        final boolean approximate
    ) throws SQLException
    {
        try
        (Statement statement = connection.createStatement()) {

            SoapResultSet r = (SoapResultSet) statement.executeQuery(
                "INDEX_INFO " +
                " CATALOG " + sqlStr(catalog) +
                " SCHEMA " + sqlStr(schema) +
                " TABLE " + sqlStr(table) +
                " UNIQUE " + unique +
                " APPROXIMATE " + approximate
            );

            r.load();

            return r;
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getJDBCMajorVersion() throws SQLException
    {
        return 1;
    }

    /** {@inheritDoc} */
    @Override
    public int getJDBCMinorVersion() throws SQLException
    {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxBinaryLiteralLength() throws SQLException
    {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxCatalogNameLength() throws SQLException
    {
        return 32;
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxCharLiteralLength() throws SQLException
    {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxColumnNameLength() throws SQLException
    {
        return 32;
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxColumnsInGroupBy() throws SQLException
    {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxColumnsInIndex() throws SQLException
    {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxColumnsInOrderBy() throws SQLException
    {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxColumnsInSelect() throws SQLException
    {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxColumnsInTable() throws SQLException
    {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxConnections() throws SQLException
    {
        return 10;
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxCursorNameLength() throws SQLException
    {
        return 32;
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxIndexLength() throws SQLException
    {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxProcedureNameLength() throws SQLException
    {
        return 32;
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxRowSize() throws SQLException
    {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxSchemaNameLength() throws SQLException
    {
        return 32;
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxStatementLength() throws SQLException
    {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxStatements() throws SQLException
    {
        return 16;
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxTableNameLength() throws SQLException
    {
        return 32;
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxTablesInSelect() throws SQLException
    {
        return 1;
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxUserNameLength() throws SQLException
    {
        return 32;
    }

    /** {@inheritDoc} */
    @Override
    public String getNumericFunctions() throws SQLException
    {
        // no additional numerical functions.
        return "";
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getPrimaryKeys(final String catalog,final String schema,final String table) throws SQLException
    {
        try
        (Statement statement = connection.createStatement()) {

            SoapResultSet r = (SoapResultSet) statement.executeQuery(
                "PRIMARY_KEYS CATALOG " +
                sqlStr(catalog) + " SCHEMA " + sqlStr(schema) + " TABLE " + sqlStr(table)
            );

            r.load();

            return r;
        }
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern) throws SQLException
    {
        try
        (Statement statement = connection.createStatement()) {

            StringBuilder buffer = new StringBuilder();
            buffer.append("PROCEDURE_COLUMNS CATALOG ").append(sqlStr(catalog)).append(" SCHEMA ").
                    append(sqlStr(schemaPattern)).append(" PROCEDURE ").append(sqlStr(procedureNamePattern)).
                    append(" COLUMN ").append(sqlStr(columnNamePattern));

            SoapResultSet r = (SoapResultSet) statement.executeQuery(
                buffer.toString()
            );

            r.load();

            return r;
        }

    }

    /** {@inheritDoc} */
    @Override
    public String getProcedureTerm() throws SQLException
    {
        return "procedure";
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getProcedures(final String catalog, final String schemaPattern, final String procedureNamePattern) throws SQLException
    {
        try
        (Statement statement = connection.createStatement()) {

            StringBuilder buffer = new StringBuilder();
            buffer.append("PROCEDURES CATALOG ").append(sqlStr(catalog)).append(" SCHEMA ").append(sqlStr(schemaPattern)).append(" PROCEDURE ").append(sqlStr(procedureNamePattern));

            SoapResultSet r = (SoapResultSet) statement.executeQuery(
                buffer.toString()
            );

            r.load();

            return r;
        }

    }

    /** {@inheritDoc} */
    @Override
    public int getResultSetHoldability() throws SQLException
    {
        return ResultSet.HOLD_CURSORS_OVER_COMMIT;
    }

    /** {@inheritDoc} */
    @Override
    public String getSQLKeywords() throws SQLException
    {
        // no additional key words.
        return "";
    }

    /** {@inheritDoc} */
    @Override
    public int getSQLStateType() throws SQLException
    {
        return 2;
    }

    /** {@inheritDoc} */
    @Override
    public String getSchemaTerm() throws SQLException
    {
        return "schema";
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getSchemas() throws SQLException
    {
        try
        (Statement statement = connection.createStatement()) {

            SoapResultSet r = (SoapResultSet) statement.executeQuery(
                "SCHEMAS"
            );

            r.load();

            return r;
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getSearchStringEscape() throws SQLException
    {
        // no additional string search functions
        return "";
    }

    /** {@inheritDoc} */
    @Override
    public String getStringFunctions() throws SQLException
    {
        // no additional string functions
        return "";
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern) throws SQLException
    {
        try
        (Statement statement = connection.createStatement()) {

            SoapResultSet r = (SoapResultSet) statement.executeQuery(
                "SUPER_TABLES CATALOG " +
                sqlStr(catalog) + " SCHEMA " + sqlStr(schemaPattern) + " TABLE " + sqlStr(tableNamePattern)
            );

            r.load();

            return r;
        }
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern) throws SQLException
    {
        try
        (Statement statement = connection.createStatement()) {

            SoapResultSet r = (SoapResultSet) statement.executeQuery(
                "SUPER_TYPES CATALOG " +
                sqlStr(catalog) + " SCHEMA " + sqlStr(schemaPattern) + " TYPE " + sqlStr(typeNamePattern)
            );

            r.load();

            return r;
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getSystemFunctions() throws SQLException
    {
        // no additional system functions.
        return "";
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern) throws SQLException
    {
        try
        (Statement statement = connection.createStatement()) {

            SoapResultSet r = (SoapResultSet) statement.executeQuery(
                "TABLE_PRIVILEGES CATALOG " +
                sqlStr(catalog) + " SCHEMA " + sqlStr(schemaPattern) + " TABLE " + sqlStr(tableNamePattern)
            );

            r.load();

            return r;
        }
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getTableTypes() throws SQLException
    {
        SResultSet r = new SResultSet( );
        r.decodeTableData("TABLE_TYPE\nTABLE");
        //throw new SoapSQLException( "getTableTypes() - Not Supported");

        return r;
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws SQLException
    {
        try
        (Statement statement = connection.createStatement()) {

            StringBuilder buffer = new StringBuilder();
            buffer.append("TABLES CATALOG ").append(sqlStr(catalog)).append(" SCHEMA ").append(sqlStr(schemaPattern)).append(" TABLE ").append(sqlStr(tableNamePattern));

            if( types != null)
            {
                buffer.append( " TYPES ");
                for( int i =0; i < types.length; i++)
                {
                    if( i > 0) buffer.append( ",");
                    buffer.append( "'");
                    buffer.append( types[i]);
                    buffer.append( "'");
                }
            }

            SoapResultSet r = (SoapResultSet) statement.executeQuery(
                buffer.toString()
            );

            r.load();

            return r;
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getTimeDateFunctions() throws SQLException
    {
        // no additional time functions
        return "";
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getTypeInfo() throws SQLException
    {
             /*  <OL>
     *  <LI><B>TYPE_NAME</B> String => Type name
     *  <LI><B>DATA_TYPE</B> int => SQL data type from java.sql.Types
     *  <LI><B>PRECISION</B> int => maximum precision
     *  <LI><B>LITERAL_PREFIX</B> String => prefix used to quote a literal
     *      (may be <code>null</code>)
     *  <LI><B>LITERAL_SUFFIX</B> String => suffix used to quote a literal
     (may be <code>null</code>)
     *  <LI><B>CREATE_PARAMS</B> String => parameters used in creating
     *      the type (may be <code>null</code>)
     *  <LI><B>NULLABLE</B> short => can you use NULL for this type.
     *      <UL>
     *      <LI> typeNoNulls - does not allow NULL values
     *      <LI> typeNullable - allows NULL values
     *      <LI> typeNullableUnknown - nullability unknown
     *      </UL>
     *  <LI><B>CASE_SENSITIVE</B> boolean=> is it case sensitive.
     *  <LI><B>SEARCHABLE</B> short => can you use "WHERE" based on this type:
     *      <UL>
     *      <LI> typePredNone - No support
     *      <LI> typePredChar - Only supported with WHERE .. LIKE
     *      <LI> typePredBasic - Supported except for WHERE .. LIKE
     *      <LI> typeSearchable - Supported for all WHERE ..
     *      </UL>
     *  <LI><B>UNSIGNED_ATTRIBUTE</B> boolean => is it unsigned.
     *  <LI><B>FIXED_PREC_SCALE</B> boolean => can it be a money value.
     *  <LI><B>AUTO_INCREMENT</B> boolean => can it be used for an
     *      auto-increment value.
     *  <LI><B>LOCAL_TYPE_NAME</B> String => localized version of type name
     *      (may be <code>null</code>)
     *  <LI><B>MINIMUM_SCALE</B> short => minimum scale supported
     *  <LI><B>MAXIMUM_SCALE</B> short => maximum scale supported
     *  <LI><B>SQL_DATA_TYPE</B> int => unused
     *  <LI><B>SQL_DATETIME_SUB</B> int => unused
     *  <LI><B>NUM_PREC_RADIX</B> int => usually 2 or 10
*/

        StringBuilder buffer = new StringBuilder();
        buffer.append(
            "TYPE_NAME\tDATA_TYPE\tPRECISION\tLITERAL_PREFIX\tLITERAL_SUFFIX\t" +
            "CREATE_PARAMS\tNULLABLE\tCASE_SENSITIVE\tSEARCHABLE\tUNSIGNED_ATTRIBUTE\t"+
            "FIXED_PREC_SCALE\tAUTO_INCREMENT\tLOCAL_TYPE_NAME\tMINIMUM_SCALE\t" +
            "MAXIMUM_SCALE\tSQL_DATA_TYPE\tSQL_DATETIME_SUB\tNUM_PREC_RADIX"
        );

        String types[][] ={
            {"TEXT",    "" + Types.CHAR,      "0", "'", "'","", "" + typeNoNulls,  "N", "" + typeSearchable, "N", "N", "N", null, "0", "0", null, null,"10"},
            {"INTEGER", "" + Types.INTEGER,   "0", "",  "", "", "" + typeNullable, "N", "" + typeSearchable, "N", "N", "N", null, "0", "0", null, null,"10"},
            {"DOUBLE",  "" + Types.FLOAT,     "0", "",  "", "", "" + typeNullable, "N", "" + typeSearchable, "N", "N", "N", null, "0", "0", null, null,"10"},
            {"DATE",    "" + Types.DATE,      "0", "'", "'","", "" + typeNullable, "N", "" + typeSearchable, "N", "N", "N", null, "0", "0", null, null,"10"},
            {"BIGINT",  "" + Types.BIGINT,    "0", "",  "", "", "" + typeNullable, "N", "" + typeSearchable, "N", "N", "N", null, "0", "0", null, null,"10"},
            {"TIMESTAMP",""+ Types.TIMESTAMP, "0", "'", "'","", "" + typeNullable, "N", "" + typeSearchable, "N", "N", "N", null, "0", "0", null, null,"10"},
            {"BOOLEAN", "" + Types.BOOLEAN,   "0", "",  "", "", "" + typeNoNulls,  "N", "" + typeSearchable, "N", "N", "N", null, "0", "0", null, null,"10"},
        };

        for (String[] line : types) {
            buffer.append( "\n");

            for( int j = 0; j < line.length; j++)
            {
                if( j > 0 ) buffer.append( "\t");
                String value = line[j];
                if( value != null)
                {
                    buffer.append( value);
                }
            }
        }

        SResultSet r = new SResultSet( );

        r.decodeTableData(buffer.toString());

        return r;
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getUDTs(
        final String catalog,
        final String schemaPattern,
        final String typeNamePattern,
        final int[] types
    ) throws SQLException
    {
        LOGGER.debug( "getUDTs(" +  catalog + "," + schemaPattern + "," + typeNamePattern + "," + Arrays.toString(types) + ")");

        SResultSet r = new SResultSet( );
    /*
     *  <LI><B>TYPE_CAT</B> String => the type's catalog (may be <code>null</code>)
     *  <LI><B>TYPE_SCHEM</B> String => type's schema (may be <code>null</code>)
     *  <LI><B>TYPE_NAME</B> String => type name
     *  <LI><B>CLASS_NAME</B> String => Java class name
     *  <LI><B>DATA_TYPE</B> int => type value defined in java.sql.Types.
     *     One of JAVA_OBJECT, STRUCT, or DISTINCT
     *  <LI><B>REMARKS</B> String => explanatory comment on the type
     *  <LI><B>BASE_TYPE</B> short => type code of the source type of a
     *     DISTINCT type or the type that implements the user-generated
     *     reference type of the SELF_REFERENCING_COLUMN of a structured
     *     type as defined in java.sql.Types (<code>null</code> if DATA_TYPE is not
     *     DISTINCT or not STRUCT with REFERENCE_GENERATION = USER_DEFINED)
     */

        r.decodeTableData("TYPE_CAT\tTYPE_SCHEM\tTYPE_NAME\tCLASS_NAME\tDATA_TYPE\tREMARKS\tBASE_TYPE");
        return r;
    }

    /** {@inheritDoc} */
    @Override
    public String getURL() throws SQLException
    {
        return connection.getExecutor().toString();
    }

    /** {@inheritDoc} */
    @Override
    public String getUserName() throws SQLException
    {
        return connection.getExecutor().getUserName();
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getVersionColumns(final String catalog, final String schema, final String table) throws SQLException
    {
        try
        (Statement statement = connection.createStatement()) {

            SoapResultSet r = (SoapResultSet) statement.executeQuery(
                "VERSION_COLUMNS CATALOG " +
                sqlStr(catalog) + " SCHEMA " + sqlStr(schema) + " TABLE " + sqlStr(table)
            );

            r.load();

            return r;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean insertsAreDetected(int type) throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCatalogAtStart() throws SQLException
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isReadOnly() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean locatorsUpdateCopy() throws SQLException
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean nullPlusNonNullIsNull() throws SQLException
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean nullsAreSortedAtEnd() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean nullsAreSortedAtStart() throws SQLException
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean nullsAreSortedHigh() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean nullsAreSortedLow() throws SQLException
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean othersDeletesAreVisible(int type) throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean othersInsertsAreVisible(int type) throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean othersUpdatesAreVisible(int type) throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean ownDeletesAreVisible(int type) throws SQLException
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean ownInsertsAreVisible(int type) throws SQLException
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean ownUpdatesAreVisible(int type) throws SQLException
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean storesLowerCaseIdentifiers() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean storesMixedCaseIdentifiers() throws SQLException
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean storesUpperCaseIdentifiers() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsANSI92EntryLevelSQL() throws SQLException
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsANSI92FullSQL() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsANSI92IntermediateSQL() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsAlterTableWithAddColumn() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsAlterTableWithDropColumn() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsBatchUpdates() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsCatalogsInDataManipulation() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsCatalogsInIndexDefinitions() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsCatalogsInProcedureCalls() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsCatalogsInTableDefinitions() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsColumnAliasing() throws SQLException
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsConvert() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsConvert(int fromType, int toType) throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsCoreSQLGrammar() throws SQLException
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsCorrelatedSubqueries() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsDataManipulationTransactionsOnly() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsDifferentTableCorrelationNames() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsExpressionsInOrderBy() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsExtendedSQLGrammar() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsFullOuterJoins() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsGetGeneratedKeys() throws SQLException
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsGroupBy() throws SQLException
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsGroupByBeyondSelect() throws SQLException
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsGroupByUnrelated() throws SQLException
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsIntegrityEnhancementFacility() throws SQLException
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsLikeEscapeClause() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsLimitedOuterJoins() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsMinimumSQLGrammar() throws SQLException
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsMixedCaseIdentifiers() throws SQLException
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsMultipleOpenResults() throws SQLException
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsMultipleResultSets() throws SQLException
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsMultipleTransactions() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsNamedParameters() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsNonNullableColumns() throws SQLException
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsOpenCursorsAcrossCommit() throws SQLException
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsOpenCursorsAcrossRollback() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsOpenStatementsAcrossCommit() throws SQLException
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsOpenStatementsAcrossRollback() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsOrderByUnrelated() throws SQLException
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsOuterJoins() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsPositionedDelete() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsPositionedUpdate() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException
    {
        if( ResultSet.TYPE_SCROLL_SENSITIVE == type)
        {
            return ResultSet.CONCUR_READ_ONLY == concurrency;
        }
        else
        {
            return false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsResultSetHoldability(int holdability) throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsResultSetType(int type) throws SQLException
    {
        return true;//ResultSet.TYPE_SCROLL_SENSITIVE == type;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsSavepoints() throws SQLException
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsSchemasInDataManipulation() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsSchemasInIndexDefinitions() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsSchemasInProcedureCalls() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsSchemasInTableDefinitions() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsSelectForUpdate() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc}
     * @throws java.sql.SQLException if a database-access error occurs.
     */
    @Override
    public boolean supportsStatementPooling() throws SQLException
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsStoredProcedures() throws SQLException
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsSubqueriesInComparisons() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsSubqueriesInExists() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsSubqueriesInIns() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsSubqueriesInQuantifieds() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsTableCorrelationNames() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsTransactionIsolationLevel(int level) throws SQLException
    {
        return Connection.TRANSACTION_READ_COMMITTED == level;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsTransactions() throws SQLException
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsUnion() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsUnionAll() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean updatesAreDetected(int type) throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean usesLocalFilePerTable() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean usesLocalFiles() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public RowIdLifetime getRowIdLifetime() throws SQLException
    {
        throw new SoapSQLException( "Not Supported");
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException
    {
        throw new SoapSQLException( "Not Supported");
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException
    {
        throw new SoapSQLException( "Not Supported");
    }

    /** {@inheritDoc} */
    @Override
    public boolean autoCommitFailureClosesAllResultSets() throws SQLException
    {
        throw new SoapSQLException( "Not Supported");
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getClientInfoProperties() throws SQLException
    {
        throw new SoapSQLException( "Not Supported");
    }

    /** {@inheritDoc}
     * @return the value
     * @throws SQLException if a database-access error occurs.
     */
    public boolean providesQueryObjectGenerator() throws SQLException
    {
        throw new SoapSQLException( "Not Supported");
    }

    /** {@inheritDoc} */
    @Override
    public Object unwrap(Class iface) throws SQLException
    {
        throw new SoapSQLException( "Not Supported");
    }

    /** {@inheritDoc} */
    @Override
    public boolean isWrapperFor(Class iface) throws SQLException
    {
        throw new SoapSQLException( "Not Supported");
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern, String columnNamePattern) throws SQLException
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
