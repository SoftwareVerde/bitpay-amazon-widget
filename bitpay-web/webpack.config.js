/* eslint-disable @typescript-eslint/explicit-function-return-type */
/* eslint-disable @typescript-eslint/no-var-requires */
const path = require('path');
const webpack = require('webpack');
const ZipPlugin = require('zip-webpack-plugin');
const TerserPlugin = require('terser-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const Dotenv = require('dotenv-webpack');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const CspHtmlWebpackPlugin = require('csp-html-webpack-plugin');
const WriteWebpackPlugin = require('write-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const ForkTsCheckerWebpackPlugin = require('fork-ts-checker-webpack-plugin');

const dotEnv = new Dotenv({
  path: process.env.NODE_ENV === 'production' ? '.env' : `.env.${process.env.NODE_ENV}`,
  defaults: true
});

const apiOrigin = dotEnv.definitions['process.env.API_ORIGIN'].replace(/"/g, '');
process.env.API_ORIGIN = apiOrigin;

const csp = require('./csp');

const sourcePath = path.join(__dirname, 'src');
const destPath = path.join(__dirname, 'dist');
const nodeEnv = process.env.NODE_ENV || 'development';

module.exports = {
  mode: nodeEnv,

  node: {
    fs: 'empty'
  },

  entry: path.join(sourcePath, 'index.tsx'),

  output: {
    path: path.join(destPath),
    filename: 'js/bundle.js',
  },

  resolve: {
    extensions: ['.ts', '.tsx', '.js', '.json']
  },

  module: {
    rules: [
      {
        test: /\.(js|ts)x?$/,
        loader: 'babel-loader',
        exclude: /node_modules/
      },
      {
        test: /\.(sa|sc|c)ss$/,
        use: [
          {
            loader: MiniCssExtractPlugin.loader // It creates a CSS file per JS file which contains CSS
          },
          {
            loader: 'css-loader', // Takes the CSS files and returns the CSS with imports and url(...) for Webpack
            options: {
              sourceMap: true
            }
          },
          {
            loader: 'postcss-loader', // For autoprefixer
            options: {
              ident: 'postcss',
              // eslint-disable-next-line global-require, @typescript-eslint/no-var-requires
              plugins: [require('autoprefixer')()]
            }
          },
          'resolve-url-loader', // Rewrites relative paths in url() statements
          'sass-loader' // Takes the Sass/SCSS file and compiles to the CSS
        ]
      }
    ]
  },

  plugins: [
    new ForkTsCheckerWebpackPlugin(),
    new webpack.EnvironmentPlugin(['NODE_ENV']),
    dotEnv,
    new HtmlWebpackPlugin({
      template: path.join(__dirname, 'public/index.html'),
      inject: 'body',
      chunks: ['popup'],
      filename: 'index.html'
    }),
    new CspHtmlWebpackPlugin(csp.cspObject, {
      enabled: true,
      hashingMethod: 'sha256',
      hashEnabled: {
        'script-src': process.env.NODE_ENV === 'production',
        'style-src': false
      },
      nonceEnabled: {
        'script-src': process.env.NODE_ENV === 'production',
        'style-src': false
      }
    }),
    // write css file(s) to build folder
    new MiniCssExtractPlugin({
      filename: 'css/[name].css'
    }),
    // copy static assets
    new CopyWebpackPlugin([
      {
        from: 'src/assets',
        to: 'assets'
      },
      {
        from: 'public',
        to: ''
      }
    ])
  ],

  optimization: {
    minimizer: [
      new TerserPlugin({
        cache: true,
        parallel: true,
        terserOptions: {
          output: {
            comments: false
          }
        },
        extractComments: false
      })
    ]
  }
};
