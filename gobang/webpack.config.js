const webpack = require('webpack')
const HtmlWebpackPlugin = require('html-webpack-plugin')

module.exports = {
  context: __dirname,
  target: 'web',
  devtool: 'source-map',

  entry: {
    index: './app/main.tsx',
  },
  output: {
    path: __dirname + '/build',
    filename: '[name].bundle.js'
  },

  resolve: {
    extensions: ['.js', '.json', '.ts', '.tsx'],
  },

  module: {
    rules: [
      {
        test: /\.tsx?$/,
        loader: 'ts-loader',
      },
      ...(process.env.NODE_ENV === 'production' ? [
        // 生产环境 空
      ] : [
        // 开发环境
        {
          test: /\.js$/,
          enforce: 'pre',
          loader: 'source-map-loader'
        }
      ]),
    ],
  },

  plugins: [
    new HtmlWebpackPlugin({
      template: './app/index.tmpl.html',
      chunks: ['index'],
    }),

    ...(process.env.NODE_ENV === 'production' ? [
      // 生产环境
      new webpack.optimize.OccurrenceOrderPlugin(),
      new webpack.optimize.UglifyJsPlugin(),
      new webpack.DefinePlugin({
        'process.env': {
          NODE_ENV: JSON.stringify('production')
        }
      }),
    ] : [
      // 开发环境
      // new webpack.HotModuleReplacementPlugin(),
    ]),
  ],

  devServer: {
    host: '0.0.0.0',
    contentBase: '',
    colors: true,
    historyApiFallback: true,
    inline: true,
    // hot: true
  }
}
