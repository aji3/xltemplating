<style scoped>
.flex {
  padding: 1px;
}
</style>

<template>
  <v-layout row wrap>
    <v-flex xs12>
      <v-layout>
        <v-flex xs1>
          <v-text-field :value="id" readonly label="ID"></v-text-field>
        </v-flex>
        <v-flex xs3>
          <v-text-field v-model="name" label="Name" required></v-text-field>
        </v-flex>
        <v-flex xs4>
          <v-text-field v-model="path" label="File Path" required></v-text-field>
        </v-flex>
        <v-flex xs1>
          <v-select v-model="type" :items="typeItems" label="Type"></v-select>
        </v-flex>
        <v-spacer></v-spacer>
        <v-btn color="primary" @click="test">Test</v-btn>
        <v-btn color="primary" @click="save">Save</v-btn>
      </v-layout>
      <v-layout>
        <v-flex xs12>
          <v-text-field v-model="description" label="Description"></v-text-field>
        </v-flex>
      </v-layout>
    </v-flex>
    <v-flex xs7 class="pa-1">
      <v-card color="grey lighten-4">
        <v-text-field v-model="contents" label="Template" multi-line rows="10" style="font-family: Inconsolata"></v-text-field>
      </v-card>

    </v-flex>
    <v-flex xs5 class="pa-1">
      <v-card color="grey lighten-4" height="100%">
        <v-text-field v-model="data" label="Data" multi-line rows="10" style="font-family: Inconsolata"></v-text-field>
      </v-card>

    </v-flex>
    <v-flex xs12>
      <v-card raised>
        <div id='result'></div>
      </v-card>
    </v-flex>

  </v-layout>

</template>

<script>
import axios from 'axios'
import { mapMutations } from 'vuex'

export default {
  asyncData (context) {
    console.log(context.params.id)
    const id = context.params.id === '_new' ? '' : context.params.id
    return axios.get(`http://localhost:8080/templatemanagement/templates/${id}`)
      .then((res) => {
        return {
          id: res.data.templateId,
          name: res.data.name,
          description: res.data.description,
          contents: res.data.content,
          data: res.data.sampleDataJsonStr,
          type: res.data.contentType == null ? 'TEXT' : res.data.contentType,
          path: res.data.path
        }
      })
  },
  mounted: function () {
    if (this.contents && this.data) {
      this.test()
    }
  },
  methods: {
    save: function () {
      if (!this.path) {
        this.path = `/template/${this.name.replace(/ /g, '_')}`
      }
      const request = {
        name: this.name,
        description: this.description,
        content: this.contents,
        sampleDataJsonStr: this.data,
        contentType: this.type,
        path: this.path
      }
      if (this.id == null || this.id === '') {
        axios.post('http://localhost:8080/templatemanagement/templates', request)
          .then((res) => {
            console.log(res)
            this.id = res.data.templateId
            console.log(this.id)
            this.addMessage({ message: `Content Saved with id: ${this.id}`, color: 'info', timeout: 5000 })
          })
      } else {
        axios.put(`http://localhost:8080/templatemanagement/templates/${this.id}`, request)
          .then((res) => {
            console.log(res)
            this.id = res.data.templateId
            console.log(this.id)
            this.addMessage({ message: 'Content Updated', color: 'info', timeout: 5000 })
          })
      }
    },
    test: function () {
      const request = {
        templateContents: this.contents,
        dataJsonStr: this.data
      }
      axios.post(`http://localhost:8080/templatemanagement/templates/_executeByContent`, request)
        .then((res) => {
          console.log(res)
          this.htmlContents = res.data
          this.renderContent()
          this.addMessage({ message: 'Template Updated', color: 'info', timeout: 5000 })
        })
        .catch((error) => {
          console.log(error.response.data.errors[0].detail)
          this.addMessageWithDialog({ ...error.response.data, color: 'error', timeout: 5000, dialogMessage: error.response.data.errors[0].detail })
        })
    },
    renderContent: function () {
      let content = this.htmlContents
      if (this.type === 'TEXT') {
        content = '<pre>' + content + '</pre>'
      }
      document.getElementById('result').innerHTML = content
    },
    ...mapMutations(['addMessage', 'addMessageWithDialog'])
  },
  data () {
    return {
      id: '',
      name: '',
      description: '',
      contents: '',
      data: '',
      path: '',
      type: 'TEXT',
      typeItems: ['TEXT', 'HTML'],
      htmlContents: '',
      messages: []
    }
  },
  watch: {
    type: function (val) {
      this.renderContent()
    }
  }
}
</script>