<template>
  <v-layout row
            wrap>
    <v-flex xs12>
      <v-data-table :items="templates">
        <template slot="headers"
                  slot-scope="props">
          <tr>
            <th width="30px">ID</th>
            <th>Name</th>
            <th>Description</th>
            <th width="100px">
              <v-btn fab
                     dark
                     color="blue"
                     small
                     to="_new">
                <v-icon dark>add</v-icon>
              </v-btn>
            </th>
          </tr>
        </template>
        <template slot="items"
                  slot-scope="props">
          <td>{{props.item.templateId}}</td>
          <td>{{props.item.name}}</td>
          <td>{{props.item.description}}</td>
          <td>
            <v-btn color="primary"
                   :to="String(props.item.templateId)">OPEN</v-btn>
          </td>
        </template>
      </v-data-table>
    </v-flex>
  </v-layout>

</template>

<script>
import axios from 'axios'
export default {
  asyncData (context) {
    return axios
      .get('http://localhost:8080/templatemanagement/templates')
      .then(res => {
        return { templates: res.data }
      })
  },
  data () {
    return {
      templates: []
    }
  },
  methods: {
    open: function (templateId) {
      console.log()
    }
  }
}
</script>