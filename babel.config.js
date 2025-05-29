const presets = [
    [
        "@babel/env",
        {
            targets: {
                edge: "13",
                ie: "11",
                firefox: "52",
                chrome: "69"

            },
            useBuiltIns: "usage",
            corejs: '3.0.0'
        }
    ]
];

const plugins = [
    "@babel/plugin-transform-modules-commonjs"
];

module.exports = { presets, plugins };