var inputFile = process.argv[2]
var outputFile = process.argv[3]

console.log("Running sass (Dart Sass) for " + inputFile + " => " + outputFile)

var sass = require('sass')
var fs = require('fs')
var path = require('path')

var result = sass.compile(inputFile, {
  loadPaths: [path.dirname(inputFile)],
  style: 'compressed',
  sourceMap: false
})

fs.mkdirSync(path.dirname(outputFile), { recursive: true })
fs.writeFileSync(outputFile, result.css)

