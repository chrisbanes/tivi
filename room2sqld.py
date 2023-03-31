import json
import os

ROOM_SCHEMA = 'data/db-room/schemas/app.tivi.data.TiviRoomDatabase/32.json'
SQLDELIGHT_SCHEME_DIR = 'data/db-sqldelight/commonMain/sqldelight/app/tivi/data/'

def prettify_sql(sql):
    return sql + ';\n'

with open(ROOM_SCHEMA) as json_file:
    data = json.loads(json_file.read())

if not os.path.exists(SQLDELIGHT_SCHEME_DIR):
    os.makedirs(SQLDELIGHT_SCHEME_DIR)

database = data['database']

for entity in database['entities']:
    entity_name = entity['tableName']

    with open(SQLDELIGHT_SCHEME_DIR + entity_name + ".sq", "w") as f:    
        create_sql = prettify_sql(entity['createSql'].replace('${TABLE_NAME}', entity_name))
        print(create_sql, file=f)

        indices = entity['indices']
        if indices:
            print('-- indices\n', file=f)

            for index in indices:
                index_sql = prettify_sql(index['createSql'].replace('${TABLE_NAME}', entity_name))
                print(index_sql, file=f)

for view in database['views']:
    view_name = view['viewName']

    with open(SQLDELIGHT_SCHEME_DIR + view_name + ".sq", "w") as f:    
        view_sql = prettify_sql(view['createSql'].replace('${VIEW_NAME}', view_name))
        print(view_sql, file=f)
