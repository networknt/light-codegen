const { injectBabelPlugin } = require('react-app-rewired');
const rewireLess = require('react-app-rewire-less');

module.exports = function override(config, env) {
    config = injectBabelPlugin(['import', { libraryName: 'antd', style: true }], config);  // change importing css to less
    config = rewireLess.withLoaderOptions({
             modifyVars: {
                 "@primary-color": "#0594cb",
                 "@body-background": "#f9f9f9",
                 "@layout-body-background": "#f9f9f9"
             },
    })(config, env);
    return config;
};