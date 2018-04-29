<template>
  <v-app id="inspire">
    <v-navigation-drawer :clipped="$vuetify.breakpoint.lgAndUp" v-model="drawer" fixed app>
      <v-list>
        <v-list-tile router :to="item.to" :key="i" v-for="(item, i) in items" exact>
          <v-list-tile-action>
            <v-icon v-html="item.icon"></v-icon>
          </v-list-tile-action>
          <v-list-tile-content>
            <v-list-tile-title v-text="item.title"></v-list-tile-title>
          </v-list-tile-content>
        </v-list-tile>
      </v-list>
    </v-navigation-drawer>
    <v-toolbar color="blue darken-3" dark app :clipped-left="$vuetify.breakpoint.lgAndUp" fixed>
      <v-toolbar-title style="width: 300px" class="ml-0 pl-3">
        <v-toolbar-side-icon @click.stop="drawer = !drawer"></v-toolbar-side-icon>
        <span class="hidden-sm-and-down">Template Management</span>
      </v-toolbar-title>
      <v-spacer></v-spacer>
    </v-toolbar>
    <v-content>
      <v-container fluid="true">
        <nuxt />
        <v-bottom-sheet hide-overlay>
          <v-btn slot="activator" color="info" fixed bottom right>
            Message
          </v-btn>
          <v-alert v-for="message in messages" :key="message.timestamp" value="true" @input="removeMessage(message)" :color="message.color" dismissible>
            {{ message.message }} <v-btn v-if="message.dialogMessage" outline @click="showDialog(message)">Detail Message</v-btn>
          </v-alert>
        </v-bottom-sheet>
        <v-snackbar :timeout="timeout" :color="color" v-model="snackbar">
          {{ snackbarMessage.message }}
          <v-btn dark flat @click.native="disableSnackbar()">Close</v-btn>
        </v-snackbar>
        <v-dialog v-model="dialog" max-width="1200px">
          <v-card>
            <pre>{{dialogMessage}}</pre>
          </v-card>
        </v-dialog>
      </v-container>
    </v-content>
  </v-app>
</template>

<script>
import { mapMutations } from 'vuex'
export default {
  data () {
    return {
      clipped: false,
      drawer: true,
      items: [
        { icon: 'apps', title: 'Top', to: '/' },
        { icon: 'list', title: 'List', to: '/templates/' },
        { icon: 'edit', title: 'Edit', to: '/templates/_new' }
      ],
      title: 'Template Management'
    }
  },
  computed: {
    snackbar: {
      get () { return this.$store.state.snackbar },
      set (val) { this.disableSnackbar() }
    },
    timeout () { return this.snackbarMessage.timeout },
    color () { return this.snackbarMessage.color },
    snackbarMessage () {
      if (this.$store.state.messages.length > 0) {
        return this.$store.state.messages[this.$store.state.messages.length - 1]
      } else {
        return ''
      }
    },
    messages () { return this.$store.state.messages },
    dialog: {
      get () { return this.$store.state.dialog },
      set (val) { this.disableDialog() }
    },
    dialogMessage () { return this.$store.state.dialogMessage }
  },
  methods: {
    ...mapMutations(['disableSnackbar', 'disableDialog', 'showDialog', 'removeMessage'])
  }
}
</script>