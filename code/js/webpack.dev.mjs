export default {
  mode: 'development',
  devServer: {
    historyApiFallback: true,
    port: 3000,
    compress: false,
    proxy: [
      {
        context: ['/api'],
        target: 'http://192.168.80.1:3000/',
        router: () => 'http://localhost:8080',
        onProxyRes: (proxyRes, req, res) => {
          console.log('onProxyRes');
          proxyRes.on('close', () => {
            console.log('on proxyRes close');
            if (!res.writableEnded) {
              res.end();
            }
          });
          res.on('close', () => {
            console.log('on res close');
            proxyRes.destroy();
          });
        },
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
      {
        test: /\.(png|svg|jpg|jpeg|gif|ico)$/,
        exclude: /node_modules/,
        use: ['file-loader?name=[name].[ext]'],
      },
    ],
  },
};
