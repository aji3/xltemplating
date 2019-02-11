//domains = entities.aggregate("entities", "domain")

// link entity with properties
//entities.each{entity -> entity.put('properties', properties.findAll(['entityName':entity.entityName]))}

// add default properties to entity
//entities.each{entity -> 
//  defaultProperties.each {defaultProperty -> 
//    entity.properties.add(defaultProperty)
//  }
//}

// create list of keys in entity
//entities.each{it.put('keys', it.properties.findAll{it.keyId != null && it.keyId.length() > 0})}

// create list of keys which are not included in the apiUrl
entities.each{entity ->
  entity.keys.each{key -> key.put('isParentKey', entity.apiUrl.indexOf('{' +key.propertyName + '}') >= 0)}
}

entities.each{entity ->
  entity.put('parentKeys', entity.keys.findAll{it.isParentKey})
} 

//codeMasterMap = codes.aggregate('codes', 'type').toMap('type')
//entities.each{ entity -> entity.put('codeMaster', codeMasterMap)}

//daoMapperPackages = entities.collect{it.daoPackage}.unique()

//$excel.domains = domains
//$excel.codeMasterMap = codeMasterMap
//$excel.daoMapperPackages = daoMapperPackages


println 'this is prescript'
println domains
