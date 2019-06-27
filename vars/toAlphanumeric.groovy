def call(Map opts = [:]) {
    opts.text.toLowerCase().replaceAll("[^a-z0-9]", "")
}