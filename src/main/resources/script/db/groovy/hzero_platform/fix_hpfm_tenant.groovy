package script.db

databaseChangeLog(logicalFilePath: 'script/db/fix_hpfm_tenant.groovy') {
    changeSet(author: 'scp', id: '2021-02-22-fix_hpfm_tenant') {
        sql("UPDATE hpfm_tenant htt SET htt.limit_user_qty = NULL WHERE htt.tenant_id IN (select tmp.tenant_id from  (SELECT ht.tenant_id FROM hpfm_tenant_config htc JOIN hpfm_tenant ht ON htc.tenant_id = ht.tenant_id AND ht.limit_user_qty IS NOT NULL WHERE htc.config_key = 'isRegister' AND htc.config_value = 'false' )tmp)")
    }
}
