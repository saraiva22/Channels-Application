export default {
  mode: 'development',
  devServer: {
    historyApiFallback: true,
    port: 3000,
    proxy: [
      {
        context: ['/api'],
        target: 'http://localhost:8080',
        router: () => 'http://localhost:8080',
      },
    ],
  },
  resolve: {
    extensions: ['.js', '.ts', '.tsx'],
  },
  plugins: [],
  module: {
    rules: [
      {
        test: /\.tsx?$/,
        use: 'ts-loader',
        exclude: /node_modules/,
      },
      {
        test: /\.css$/i,
        use: ['style-loader', 'css-loader'],
      },
    ],
  },
};
