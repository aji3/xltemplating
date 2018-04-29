export const state = () => ({
  sidebar: false,
  messages: [],
  snackbar: false,
  dialog: false,
  dialogMessage: ''
})

export const mutations = {
  toggleSidebar (state) {
    state.sidebar = !state.sidebar
  },
  addMessage (state, message) {
    message.timestamp = new Date().getTime()
    state.messages.push(message)
    state.snackbar = true
  },
  addMessageWithDialog (state, message) {
    message.timestamp = new Date().getTime()
    state.messages.push(message)
    state.snackbar = true
    state.dialog = true
    state.dialogMessage = message.dialogMessage
  },
  removeMessage (state, message) {
    state.messages.splice(state.messages.indexOf(message), 1)
  },
  showDialog (state, message) {
    state.dialog = true
    state.dialogMessage = message.dialogMessage
  },
  disableSnackbar (state) {
    state.snackbar = false
  },
  disableDialog (state) {
    state.dialog = false
  }
}
