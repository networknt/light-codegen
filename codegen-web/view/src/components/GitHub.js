import React, {Component} from 'react';
import {withStyles} from "@material-ui/core";

const styles = theme => ({
    root: {
        display: 'flex',
        flexWrap: 'wrap',
    },
    pre: {
        'white-space': 'pre-wrap',
    },
});

class GitHub extends Component {
    render() {
        const { classes } = this.props;
        return (
            <div>
                <pre className={classes.pre}>
                    Light-codegen is an open source project on GitHub.com
                </pre>
                <pre className={classes.pre}>
                    For the light-codegen project, please visit <a href="https://github.com/networknt/light-codegen" rel="noreferrer noopener" target="_blank">light-codegen</a>.
                </pre>
                <pre className={classes.pre}>
                    For the services to support this site, please visit <a href="https://github.com/networknt/light-codegen/tree/master/codegen-web" rel="noreferrer noopener" target="_blank">codegen-web</a>.
                </pre>
                <pre className={classes.pre}>
                    For the react single page application, please visit <a href="https://github.com/networknt/light-codegen/tree/master/codegen-web/view" rel="noreferrer noopener" target="_blank">view</a>.
                </pre>
            </div>
        )
    }
}

export default withStyles(styles)(GitHub);